(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('EliminationDialogController', EliminationDialogController);

    EliminationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Elimination', 'Participant', 'filterFilter'];

    function EliminationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Elimination, Participant, filterFilter) {
        var vm = this;

        vm.elimination = entity;
        
        vm.clear = clear;
        vm.save = save;
        
         vm.participants = Participant.query();
        
//        initiating default values 
        vm.elimination.bronzeMatch = vm.elimination.bronzeMatch || false;
        vm.elimination.type = vm.elimination.type || "SINGLE";
        vm.elimination.participants = vm.elimination.participants || [];
        vm.selectedPlayers = filterFilter(vm.elimination.participants, {team : null});
        vm.selectedTeams = filterFilter(vm.elimination.participants, {player : null});
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
        
        function selectParticipants() {
            console.log("Selecting participants");
            if($scope.chosen === 1){
                angular.copy(vm.selectedPlayers, vm.elimination.participants);
                console.log("vm.selectedPlayers: " + vm.selectedPlayers);
                console.log("vm.elimination.participants: " + vm.elimination.participants);
            }
            if($scope.chosen === 2){
                angular.copy(vm.selectedTeams, vm.elimination.participants);
                console.log("vm.selectedTeams: " + vm.selectedTeams);
                console.log("vm.elimination.participants: " + vm.elimination.participants);
            }
        }

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            selectParticipants();
            if (vm.elimination.id !== null) {
                Elimination.update(vm.elimination, onSaveSuccess, onSaveError);
            } else {
                Elimination.save(vm.elimination, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:eliminationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
