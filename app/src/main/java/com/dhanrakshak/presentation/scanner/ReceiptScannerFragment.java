package com.dhanrakshak.presentation.scanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dhanrakshak.databinding.FragmentReceiptScannerBinding;
import com.dhanrakshak.util.ReceiptParser;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiptScannerFragment extends Fragment {

    private static final String TAG = "ReceiptScanner";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[] { Manifest.permission.CAMERA };

    private FragmentReceiptScannerBinding binding;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentReceiptScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        binding.btnCapture.setOnClickListener(v -> takePhotoAndProcess());

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider
                .getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                } catch (Exception exc) {
                    Log.e(TAG, "Use case binding failed", exc);
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera provider capture failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhotoAndProcess() {
        if (imageCapture == null)
            return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCapture.setEnabled(false);

        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull androidx.camera.core.ImageProxy image) {
                        processImage(image);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnCapture.setEnabled(true);
                    }
                });
    }

    @androidx.camera.core.ExperimentalGetImage
    private void processImage(androidx.camera.core.ImageProxy imageProxy) {
        if (imageProxy.getImage() == null)
            return;

        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees());
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String rawText = visionText.getText();
                    ReceiptParser.ReceiptData data = ReceiptParser.parse(rawText);

                    showResultDialog(data, rawText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text recognition failed", e);
                    Toast.makeText(requireContext(), "Failed to read receipt", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    imageProxy.close();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnCapture.setEnabled(true);
                });
    }

    private void showResultDialog(ReceiptParser.ReceiptData data, String rawText) {
        // Determine context of "Merchant" - fallback to "Unknown" if null
        String merchant = data.merchant != null ? data.merchant : "Unknown Merchant";
        // Determine amount - fallback to 0

        String msg = "Merchant: " + merchant + "\n" +
                "Date: " + (data.date != null ? data.date : "N/A") + "\n" +
                "Total: " + data.amount;

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Receipt Scanned")
                .setMessage(msg)
                .setPositiveButton("Add Transaction", (dialog, which) -> {
                    // TODO: Navigate to Add Transaction Fragment with pre-filled data
                    // For now, just show toast
                    Toast.makeText(requireContext(), "Transaction Added: " + data.amount, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Retake", null)
                .setNeutralButton("Show Raw", (dialog, which) -> {
                    new android.app.AlertDialog.Builder(requireContext())
                            .setMessage(rawText)
                            .show();
                })
                .show();
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
