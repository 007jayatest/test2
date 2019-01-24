(function ($) {
    'use strict';

    function create(type, options) {
        var defer = $.Deferred();

        var announcement = $('<div class="announce">');
        var timeout;
        var longerSaveTimeout;

        if (typeof (options) === 'string') {
            options = {message: options};
        }

        options = $.extend({}, $.announce.defaults, options);

        // Remove existing announcements
        $(".eui-inline-notification").empty();

        // Create the announcement
        $(announcement)
                .append('<span class="aui-icon aui-icon-small ' + options.className + ' eui-announce-' + type + '">Progress: </span>');

        // Hide on click
        if (options.hideOnClick) {
            $(announcement).on('click.announce', function () {
                clearTimeout(timeout);
                options.hide.call(announcement).then(defer.resolve);
            });
        }

        // Set the message
        if (options.html) {
            $(announcement).append(options.message);
        } else {
            $(announcement).append("<span class='eui-vertical-align'>" + options.message[0] + "</span>");
        }

        // Add it to the DOM
        $('.eui-inline-notification').append(announcement);

        // Show it
        options.show.call(announcement);

        // Hide after a moment by default it's false
        if (options.autoHide) {
            timeout = setTimeout(function () {
                options.hide.call(announcement).then(defer.resolve);
            }, options.duration);
        }

        // Change message to longer save message
        clearTimeout(longerSaveTimeout);
        // Change the message after 3 seconds
        if (options.message.length > 1) {
            longerSaveTimeout = setTimeout(function () {
                jQuery(announcement).children(".eui-vertical-align").text(options.message[1]);
            }, options.longSaveDuration);
        }
        return defer;
    }
    
    function hide() {
        $(".eui-inline-notification").empty();
    }
    
    $.announce = {
        // Default options
        defaults: {
            className: 'aui-iconfont-devtools-task-in-progress',
            duration: 2000,
            longSaveDuration: 3000,
            hideOnClick: true,
            html: false,
            autoHide: false,
            show: function () {
                var defer = $.Deferred();
                $(this).fadeIn(250, function () {
                    defer.resolve();
                });
                return defer;
            },
            hide: function () {
                var defer = $.Deferred();
                $(this).fadeOut(200, function () {
                    $(this).remove();
                    defer.resolve();
                });
                return defer;
            }
        },

        // Hide
        hide: function () {
            return hide();
        },
        // Info
        info: function (options) {
            return create('info', options);
        },

        // Error
        error: function (options) {
            return create('error', options);
        },

        // Success
        success: function (options) {
            return create('success', options);
        },

        // Warning
        warning: function (options) {
            return create('warning', options);
        },

        // Custom announcement
        say: function (type, options) {
            return create(type, options);
        }
    };
})(jQuery);