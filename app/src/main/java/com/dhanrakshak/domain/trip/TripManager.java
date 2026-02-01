package com.dhanrakshak.domain.trip;

import android.content.Context;

import com.dhanrakshak.data.local.dao.TripDao;
import com.dhanrakshak.data.local.dao.TripExpenseDao;
import com.dhanrakshak.data.local.dao.TripLocationDao;
import com.dhanrakshak.data.local.entity.Trip;
import com.dhanrakshak.data.local.entity.TripExpense;
import com.dhanrakshak.data.local.entity.TripLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Trip Manager for comprehensive trip planning and expense tracking.
 * Handles journey analytics, expense summaries, and location timeline.
 */
@Singleton
public class TripManager {

    private final Context context;
    private final TripDao tripDao;
    private final TripExpenseDao expenseDao;
    private final TripLocationDao locationDao;

    // Currency exchange rates (simplified - would fetch from API in production)
    private static final Map<String, Double> EXCHANGE_RATES = new HashMap<String, Double>() {
        {
            put("INR", 1.0);
            put("USD", 83.12);
            put("EUR", 90.50);
            put("GBP", 105.20);
            put("THB", 2.35);
            put("SGD", 62.10);
            put("AED", 22.64);
            put("JPY", 0.56);
        }
    };

    @Inject
    public TripManager(Context context, TripDao tripDao,
            TripExpenseDao expenseDao, TripLocationDao locationDao) {
        this.context = context.getApplicationContext();
        this.tripDao = tripDao;
        this.expenseDao = expenseDao;
        this.locationDao = locationDao;
    }

    /**
     * Create a new trip.
     */
    public Single<Long> createTrip(String name, String destination,
            long startDate, long endDate, double budget) {
        Trip trip = new Trip(name, destination, startDate, endDate, budget);
        return tripDao.insert(trip);
    }

    /**
     * Add expense to a trip.
     */
    public Completable addExpense(long tripId, String description, double amount,
            String currency, String category) {
        TripExpense expense = new TripExpense(tripId, description, amount, category);
        expense.setCurrency(currency);
        expense.setAmountInInr(convertToInr(amount, currency));

        return expenseDao.insert(expense)
                .andThen(updateTripTotal(tripId));
    }

    /**
     * Add location waypoint to trip.
     */
    public Completable addLocation(long tripId, String name, double lat, double lng,
            String locationType, int tripDay) {
        return locationDao.getMaxSequenceOrder(tripId)
                .onErrorReturnItem(0)
                .flatMapCompletable(maxOrder -> {
                    TripLocation location = new TripLocation(tripId, name, lat, lng);
                    location.setLocationType(locationType);
                    location.setTripDay(tripDay);
                    location.setSequenceOrder(maxOrder + 1);
                    return locationDao.insert(location);
                });
    }

    /**
     * Update trip spent total.
     */
    private Completable updateTripTotal(long tripId) {
        return expenseDao.getTotalForTrip(tripId)
                .flatMapCompletable(
                        total -> tripDao.updateSpent(tripId, total != null ? total : 0, System.currentTimeMillis()));
    }

    /**
     * Get trip summary with analytics.
     */
    public Single<TripSummary> getTripSummary(long tripId) {
        return Single.zip(
                tripDao.getById(tripId),
                expenseDao.getTotalForTrip(tripId).onErrorReturnItem(0.0),
                expenseDao.getCategoryWiseTotals(tripId).onErrorReturnItem(new ArrayList<>()),
                expenseDao.getExpenseCount(tripId).onErrorReturnItem(0),
                locationDao.getLocationCount(tripId).onErrorReturnItem(0),
                (trip, total, categories, expenseCount, locationCount) -> {
                    Map<String, Double> categoryMap = new HashMap<>();
                    for (TripExpenseDao.CategoryTotal ct : categories) {
                        categoryMap.put(ct.category, ct.total);
                    }
                    return new TripSummary(trip, total, categoryMap, expenseCount, locationCount);
                }).subscribeOn(Schedulers.io());
    }

    /**
     * Get daily expense breakdown.
     */
    public Single<List<DayExpenseSummary>> getDailyBreakdown(long tripId) {
        return tripDao.getById(tripId)
                .flatMap(trip -> {
                    List<Single<DayExpenseSummary>> daySingles = new ArrayList<>();
                    for (int day = 1; day <= trip.getDurationDays(); day++) {
                        int dayNum = day;
                        Single<DayExpenseSummary> daySummary = expenseDao.getTotalForDay(tripId, day)
                                .onErrorReturnItem(0.0)
                                .map(total -> new DayExpenseSummary(dayNum, total));
                        daySingles.add(daySummary);
                    }
                    return Single.zip(daySingles, objects -> {
                        List<DayExpenseSummary> summaries = new ArrayList<>();
                        for (Object obj : objects) {
                            summaries.add((DayExpenseSummary) obj);
                        }
                        return summaries;
                    });
                }).subscribeOn(Schedulers.io());
    }

    /**
     * Calculate total journey distance.
     */
    public Single<Double> calculateTotalDistance(long tripId) {
        return locationDao.getLocationsForTrip(tripId)
                .firstOrError()
                .map(locations -> {
                    double totalDistance = 0;
                    for (int i = 0; i < locations.size() - 1; i++) {
                        totalDistance += locations.get(i).distanceTo(locations.get(i + 1));
                    }
                    return totalDistance;
                });
    }

    /**
     * Get trip statistics for all time.
     */
    public Single<OverallTripStats> getOverallStats() {
        return Single.zip(
                tripDao.getCompletedTripCount().onErrorReturnItem(0),
                tripDao.getTotalSpentOnTrips().onErrorReturnItem(0.0),
                (count, total) -> new OverallTripStats(count, total));
    }

    /**
     * Start trip (change status to ONGOING).
     */
    public Completable startTrip(long tripId) {
        return tripDao.updateStatus(tripId, "ONGOING", System.currentTimeMillis());
    }

    /**
     * Complete trip.
     */
    public Completable completeTrip(long tripId) {
        return tripDao.updateStatus(tripId, "COMPLETED", System.currentTimeMillis());
    }

    /**
     * Convert currency to INR.
     */
    public double convertToInr(double amount, String currency) {
        Double rate = EXCHANGE_RATES.get(currency);
        if (rate == null)
            rate = 1.0;
        return amount * rate;
    }

    /**
     * Get packing checklist suggestions based on destination.
     */
    public List<String> getPackingListSuggestions(String destination, String tripType, int days) {
        List<String> items = new ArrayList<>();

        // Essential items
        items.add("🪪 ID Proof / Passport");
        items.add("💳 Cards & Cash");
        items.add("📱 Phone & Charger");
        items.add("💊 Medicines");

        // Clothing based on duration
        items.add("👕 Clothes (" + Math.min(days, 5) + " sets)");
        items.add("👟 Comfortable Shoes");
        items.add("🧥 Jacket/Sweater");

        // Toiletries
        items.add("🪥 Toiletries Kit");
        items.add("🧴 Sunscreen");

        // Tech
        items.add("📷 Camera");
        items.add("🔋 Power Bank");

        // Trip type specific
        if ("BUSINESS".equals(tripType)) {
            items.add("💼 Laptop");
            items.add("👔 Formal Wear");
        } else if ("FAMILY".equals(tripType)) {
            items.add("🎮 Entertainment for kids");
            items.add("🍼 Snacks");
        }

        // Beach destination
        String destLower = destination.toLowerCase();
        if (destLower.contains("goa") || destLower.contains("beach") ||
                destLower.contains("maldives") || destLower.contains("thailand")) {
            items.add("🩱 Swimwear");
            items.add("🕶️ Sunglasses");
            items.add("🩴 Flip Flops");
        }

        // Mountain destination
        if (destLower.contains("manali") || destLower.contains("shimla") ||
                destLower.contains("ladakh") || destLower.contains("kashmir")) {
            items.add("🧤 Gloves");
            items.add("🧣 Warm Clothes");
            items.add("🥾 Trekking Shoes");
        }

        return items;
    }

    // Data classes
    public static class TripSummary {
        public Trip trip;
        public double totalSpent;
        public Map<String, Double> categoryBreakdown;
        public int expenseCount;
        public int locationCount;
        public double remainingBudget;
        public double dailyAverage;

        public TripSummary(Trip trip, double total, Map<String, Double> categories,
                int expenseCount, int locationCount) {
            this.trip = trip;
            this.totalSpent = total;
            this.categoryBreakdown = categories;
            this.expenseCount = expenseCount;
            this.locationCount = locationCount;
            this.remainingBudget = trip.getPlannedBudget() - total;
            this.dailyAverage = trip.getDurationDays() > 0 ? total / trip.getDurationDays() : 0;
        }
    }

    public static class DayExpenseSummary {
        public int day;
        public double total;

        public DayExpenseSummary(int day, double total) {
            this.day = day;
            this.total = total;
        }
    }

    public static class OverallTripStats {
        public int totalTrips;
        public double totalSpent;
        public double averagePerTrip;

        public OverallTripStats(int trips, double spent) {
            this.totalTrips = trips;
            this.totalSpent = spent;
            this.averagePerTrip = trips > 0 ? spent / trips : 0;
        }
    }
}
