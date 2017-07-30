(function() {
    'use strict';
    angular
        .module('tournamentControlApp')
        .factory('GameSet', GameSet);

    GameSet.$inject = ['$resource'];

    function GameSet ($resource) {
        var resourceUrl =  'api/game-sets/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
