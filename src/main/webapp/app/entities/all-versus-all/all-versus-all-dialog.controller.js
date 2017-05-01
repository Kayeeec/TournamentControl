(function () {
    'use strict';

    angular
            .module('tournamentControlApp')
            .controller('AllVersusAllDialogController', AllVersusAllDialogController);

    AllVersusAllDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'AllVersusAll', 'Participant', 'filterFilter', 'SetSettings'];

    function AllVersusAllDialogController($timeout, $scope, $stateParams, $uibModalInstance, entity, AllVersusAll, Participant, filterFilter, SetSettings) {
        var vm = this;

        vm.allVersusAll = entity;
        vm.clear = clear;
        vm.save = save;
        vm.participants = Participant.query();
        
//        initiating default values 
        if(vm.allVersusAll.tiesAllowed === null){ vm.allVersusAll.tiesAllowed = true; }
        vm.allVersusAll.participants = vm.allVersusAll.participants || [];
        vm.allVersusAll.numberOfMutualMatches = vm.allVersusAll.numberOfMutualMatches || 1;
        vm.selectedPlayers = filterFilter(vm.allVersusAll.participants, {team : null});
        vm.selectedTeams = filterFilter(vm.allVersusAll.participants, {player : null});
        $scope.chosen = 1;
        vm.setSettings = {id: null, maxScore: null, minReachedScore: null, leadByPoints: null} || vm.allVersusAll.setSettings;

        $timeout(function () {
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
        
        function selectParticipants() {
            console.log("Selecting participants");
            if($scope.chosen === 1){
                angular.copy(vm.selectedPlayers, vm.allVersusAll.participants);
                console.log("vm.selectedPlayers: " + vm.selectedPlayers);
                console.log("vm.allVersusAll.participants: " + vm.allVersusAll.participants);
            }
            if($scope.chosen === 2){
                angular.copy(vm.selectedTeams, vm.allVersusAll.participants);
                console.log("vm.selectedTeams: " + vm.selectedTeams);
                console.log("vm.allVersusAll.participants: " + vm.allVersusAll.participants);
            }
        }
        

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function save() {
            
            vm.isSaving = true;
            
            selectParticipants();
            
//            if ((vm.setSettings.maxScore || vm.setSettings.minReachedScore || vm.setSettings.leadByPoints) !== null ){
//                if (vm.setSettings.id !== null) {
//                    SetSettings.update(vm.setSettings);
//                } else {
//                    SetSettings.save(vm.setSettings);
//                }
//                
//            }
            
            if (vm.allVersusAll.id !== null) {
                AllVersusAll.update(vm.allVersusAll, onSaveSuccess, onSaveError);
            } else {
                AllVersusAll.save(vm.allVersusAll, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess(result) {
            $scope.$emit('tournamentControlApp:allVersusAllUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }
        
        $scope.maxPlayingFields = function () {
            if($scope.chosen === 1){
                return Math.floor(vm.selectedPlayers.length/2);
            }
            return Math.floor(vm.selectedTeams.length/2);
        };




    }
})();
