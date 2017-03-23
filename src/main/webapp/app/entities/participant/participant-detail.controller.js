(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('ParticipantDetailController', ParticipantDetailController);

    ParticipantDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Participant', 'Player', 'Team', 'User'];

    function ParticipantDetailController($scope, $rootScope, $stateParams, previousState, entity, Participant, Player, Team, User) {
        var vm = this;

        vm.participant = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('tournamentControlApp:participantUpdate', function(event, result) {
            vm.participant = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
