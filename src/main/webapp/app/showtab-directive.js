(function () {
    'use strict';

    angular
            .module('tournamentControlApp')
            .directive('showtab', showtab);

    function showtab() {
        return {
            link: function (scope, element, attrs) {
                element.click(function (e) {
                    e.preventDefault();
                    $(element).tab('show');
                });
            }
        };
    }
    ;




})();



