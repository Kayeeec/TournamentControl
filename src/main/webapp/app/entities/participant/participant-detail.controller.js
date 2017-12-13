(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('ParticipantDetailController', ParticipantDetailController);

    ParticipantDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Participant', 'Player', 'Team', 'My'];

    function ParticipantDetailController($scope, $rootScope, $stateParams, previousState, entity, Participant, Player, Team, My) {
        var vm = this;

        vm.participant = entity;
        vm.backLink = function () {
            return My.backLink(previousState);
        };
        My.savePreviousUrl(previousState);

        var unsubscribe = $rootScope.$on('tournamentControlApp:participantUpdate', function(event, result) {
            vm.participant = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
