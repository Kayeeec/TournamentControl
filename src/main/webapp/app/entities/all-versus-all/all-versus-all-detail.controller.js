(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('AllVersusAllDetailController', AllVersusAllDetailController);

    AllVersusAllDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'AllVersusAll', 'Game', 'User', 'Participant'];

    function AllVersusAllDetailController($scope, $rootScope, $stateParams, previousState, entity, AllVersusAll, Game, User, Participant) {
        var vm = this;

        vm.allVersusAll = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('tournamentControlApp:allVersusAllUpdate', function(event, result) {
            vm.allVersusAll = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
