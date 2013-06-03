define([
    'utilities',
    'text!../../../../templates/mobile/booking-details.html'
], function (
    utilities,
    bookingDetailsTemplate) {

    var BookingDetailView = Backbone.View.extend({
        render:function () {
            utilities.applyTemplate($(this.el), bookingDetailsTemplate, this.model.attributes);
            $(this.el).trigger('pagecreate');
            return this;
        }
    });

    return BookingDetailView;
});