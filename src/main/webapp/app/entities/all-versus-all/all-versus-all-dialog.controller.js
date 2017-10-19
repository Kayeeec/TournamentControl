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
        
        vm.participantsTouched = false;
        
//        initiating default values 
        if(vm.allVersusAll.tiesAllowed === null){ vm.allVersusAll.tiesAllowed = true; }
        vm.allVersusAll.numberOfMutualMatches = vm.allVersusAll.numberOfMutualMatches || 1;
        
        /* participants stuff */
        vm.allVersusAll.participants = vm.allVersusAll.participants || [];
        vm.selectedPlayers = filterFilter(vm.allVersusAll.participants, {team : null});
        vm.selectedTeams = filterFilter(vm.allVersusAll.participants, {player : null});
        $scope.chosen = 1;
        
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
        $scope.isPlayer = function (participant) {
            if(participant.player !== null) return true;
            return false;
        };
        $scope.isTeam = function (participant) {
            if(participant.team !== null) return true;
            return false;
        };
        /* END - participants stuff */
        
        /* setSettings stuff */
        vm.setSettingsChosen = initSetSettingsChosen();
        vm.preparedSettings = vm.allVersusAll.setSettings 
                || {id: null, maxScore: null, leadByPoints: null, minReachedPoints: null};
        
        function initSetSettingsChosen() {
            if (vm.allVersusAll.id !== null && vm.allVersusAll.setSettings !== null) {
                if(vm.allVersusAll.setSettings.leadByPoints !== null 
                    || vm.allVersusAll.setSettings.minReachedScore !== null){
                    return 'leadByPoints';
                }
                return 'maxScore';
            }
            return null;
        }
        
        function resolveSetSettings() {
            if (vm.setSettingsChosen === 'maxScore') {
                vm.preparedSettings.leadByPoints = null;
                vm.preparedSettings.minReachedScore = null;
            }
            if (vm.setSettingsChosen === 'leadByPoints') {
                vm.preparedSettings.maxScore = null;
            }
            if (vm.preparedSettings.id !== null ||
                vm.preparedSettings.maxScore !== null ||
                vm.preparedSettings.leadByPoints !== null ||
                vm.preparedSettings.minReachedScore !== null){
                    vm.allVersusAll.setSettings = vm.preparedSettings;
                    console.log(vm.allVersusAll.setSettings);
            }
        }
        /* END - setSettings stuff */

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
        
        function clear() {
            $uibModalInstance.dismiss('cancel');
        }
        
        function save() {
            
            vm.isSaving = true;
            
            selectParticipants();
            resolveSetSettings();
            
//            if(vm.allVersusAll.playingFields 
//                    && vm.allVersusAll.playingFields > Math.floor(vm.allVersusAll.participants.length/2)){
//                console.log("setting fields to false");
//                vm.isSaving = false;
//                return $scope.editForm.playingFields.$setValidity= ("maxPlayingFields", false);
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
        
        /** Playing Fields Validation **/
        vm.maxPlayingFields = function () {
            if($scope.chosen===1){//players
                return Math.floor(vm.selectedPlayers.length/2);
            }
            else {//teams
                return Math.floor(vm.selectedTeams.length/2);
            }
        };
        
        vm.playingFieldsInvalid = function () {
            return vm.participantsTouched && vm.allVersusAll.playingFields > vm.maxPlayingFields();
        };
        /** end - Playing Fields Validation **/
    }
})();
