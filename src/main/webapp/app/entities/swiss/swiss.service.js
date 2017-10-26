(function() {
    'use strict';
    angular
        .module('tournamentControlApp')
        .factory('Swiss', Swiss);

    Swiss.$inject = ['$resource'];

    function Swiss ($resource) {
        var resourceUrl =  'api/swisses/:id';

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
            'update': { method:'PUT' },
            'getSeeding':{
                method: 'GET',
                url: 'api/swisses/seeding/:id',
                isArray: true
                
            },
            'generateNextRound':{
                method:'POST',
                url:'api/swisses/generate'
            }
        });
    }
})();
