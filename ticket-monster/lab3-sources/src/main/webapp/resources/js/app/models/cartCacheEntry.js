/**
 * Module for the Booking model
 */
define([
    // Configuration is a dependency
    'configuration',
    'backbone'
], function (config) {

    /**
     * The CacheEntry model class definition
     * Used for CRUD operations against individual bookings
     */
    var CartCacheEntry = Backbone.Model.extend({
        urlRoot: config.baseUrl + 'rest/caches/TICKETMONSTER_CARTS'
    });

    return CartCacheEntry;

});