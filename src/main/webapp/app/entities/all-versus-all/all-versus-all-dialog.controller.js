(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('AllVersusAllDialogController', AllVersusAllDialogController);

    AllVersusAllDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'AllVersusAll', 'Participant'];

    function AllVersusAllDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, AllVersusAll, Participant) {
        var vm = this;

        vm.allVersusAll = entity;
        vm.clear = clear;
        vm.save = save;
        vm.allVersusAll.numberOfMutualMatches = 1;
        
        vm.participants = Participant.query();
        $scope.selectedPlayers;
        $scope.selectedTeams;
        $scope.chosen = 1;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });
        
        $('#myTab a[href="#players"]').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });
        $('#myTab a[href="#teams"]').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });
        

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            
            vm.isSaving = true;
            
//            if ($scope.chosen === 1){
//                vm.allVersusAll.participants = $scope.selectedPlayers;
//            }else {
//                vm.allVersusAll.participants = $scope.selectedTeams;
//            }
//            
            
            if (vm.allVersusAll.id !== null) {
                AllVersusAll.update(vm.allVersusAll, onSaveSuccess, onSaveError);
            } else {
                AllVersusAll.save(vm.allVersusAll, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:allVersusAllUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
