(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TeamDetailController', TeamDetailController);

    TeamDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Team', 'User', 'Player'];

    function TeamDetailController($scope, $rootScope, $stateParams, previousState, entity, Team, User, Player) {
        var vm = this;

        vm.team = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('tournamentControlApp:teamUpdate', function(event, result) {
            vm.team = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
