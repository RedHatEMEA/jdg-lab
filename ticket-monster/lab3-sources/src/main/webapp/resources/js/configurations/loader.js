//detect the appropriate module to load
define(function () {

    /*
     A simple check on the client. For touch devices or small-resolution screens)
     show the mobile client. By enabling the mobile client on a small-resolution screen
     we allow for testing outside a mobile device (like for example the Mobile Browser
     simulator in JBoss Tools and JBoss Developer Studio).
     */

    var environment;

    if (navigator.userAgent == "TicktetMonster Cordova Webview iOS") {
        environment = "hybrid-ios"
    }
    else if (navigator.userAgent == "TicketMonster Cordova Webview Android") {
        environment = "hybrid-android"
    }
    else if (Modernizr.touch || Modernizr.mq("only all and (max-width: 480px)")) {
        environment = "mobile"
    } else {
        environment = "desktop"
    }

    require([environment]);
});