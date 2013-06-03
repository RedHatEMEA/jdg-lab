/**
 * The module for a collection of Bookings
 */
define([
    'app/models/cartCacheEntry',
    'configuration',
    'backbone'
], function (CartCacheEntry, config) {

    // Here we define the cacheEntries collection
    // We will use it for CRUD operations on Bookings

    var CartCacheEntries = Backbone.Collection.extend({
        url: config.baseUrl + 'rest/caches/TICKETMONSTER_CARTS',
        model: CartCacheEntry,
        id:'id'
    });

    return CartCacheEntries;
});