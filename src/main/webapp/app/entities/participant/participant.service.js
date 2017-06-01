(function () {
    'use strict';
    angular
            .module('tournamentControlApp')
            .factory('Participant', Participant);

    Participant.$inject = ['$resource'];

    function Participant($resource) {
        var resourceUrl = 'api/participants/:id';

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
            'update': {method: 'PUT'}
        });
    }

//    function Participant() {
//        return {
//            'getName': function (participant) {
//                if (participant !== null) {
//                    if (participant.player !== null) {
//                        return participant.player.name;
//                    }
//                    if (participant.team !== null) {
//                        return participant.team.name;
//                    }
//                    return 'BYE';
//                }
//                return '-';
//            },
//            'isBYE': function (rival) {
//                return (rival !== null && rival.player === null && rival.team === null);
//            }
//            
//        };
//    }
})();
