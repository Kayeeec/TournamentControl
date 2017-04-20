(function () {
    'use strict';
    angular
            .module('tournamentControlApp')
            .factory('Game', Game);

    Game.$inject = ['$resource'];

    function Game($resource) {
        var resourceUrl = 'api/games/:id';

        return $resource(resourceUrl, {}, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': {method: 'PUT'},
            'addSet': {method: 'POST',
                params: {id: '@id'},
                url: 'api/games/:id/sets',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            }
            
        });
    }
})();
