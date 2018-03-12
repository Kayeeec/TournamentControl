(function() {
    'use strict';
    angular
        .module('tournamentControlApp')
        .factory('AllVersusAll', AllVersusAll);

    AllVersusAll.$inject = ['$resource'];

    function AllVersusAll ($resource) {
        var resourceUrl =  'api/all-versus-alls/:id';

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
