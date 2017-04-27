(function() {
    'use strict';
    angular
        .module('tournamentControlApp')
        .factory('SetSettings', SetSettings);

    SetSettings.$inject = ['$resource'];

    function SetSettings ($resource) {
        var resourceUrl =  'api/set-settings/:id';

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
