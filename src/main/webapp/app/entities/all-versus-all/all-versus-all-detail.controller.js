(function () {
    'use strict';

    angular
            .module('tournamentControlApp')
            .controller('AllVersusAllDetailController', AllVersusAllDetailController);

    AllVersusAllDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'AllVersusAll', 'Game', 'Combined', 'My'];

    function AllVersusAllDetailController($scope, $rootScope, $stateParams, previousState, entity, AllVersusAll, Game, Combined, My) {
        var vm = this;

        vm.allVersusAll = entity;
        
        My.savePreviousUrl(previousState);
        
        vm.backLink = function () {
            return My.backLink(previousState);
        };
        
        if(vm.allVersusAll.inCombined){
            vm.combined = Combined.findByTournament(vm.allVersusAll); 
        }
        
        var unsubscribe = $rootScope.$on('tournamentControlApp:allVersusAllUpdate', function (event, result) {
            vm.allVersusAll = result;
            $scope.counts = [];
            console.log('on update counts: ' + $scope.counts);
        });
        $scope.$on('$destroy', unsubscribe);
        
        console.log("previousState: ", previousState);
    }
})();
