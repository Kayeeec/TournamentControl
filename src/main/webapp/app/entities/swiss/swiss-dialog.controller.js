(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('SwissDialogController', SwissDialogController);

    SwissDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Swiss', 'filterFilter', 'SetSettings', 'Participant'];

    function SwissDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Swiss, filterFilter, SetSettings, Participant) {
        var vm = this;

        vm.swiss = entity;
        vm.clear = clear;
        vm.save = save;
        
        /* participants stuff */
        vm.participants = Participant.query();
        vm.swiss.participants = vm.swiss.participants || [];
        vm.selectedPlayers = filterFilter(vm.swiss.participants, {team : null});
        vm.selectedTeams = filterFilter(vm.swiss.participants, {player : null});
        $scope.chosen = 1;
        
        function selectParticipants() {
            console.log("Selecting participants");
            if($scope.chosen === 1){
                angular.copy(vm.selectedPlayers, vm.swiss.participants);
                console.log("vm.selectedPlayers: " + vm.selectedPlayers);
                console.log("vm.swiss.participants: " + vm.swiss.participants);
            }
            if($scope.chosen === 2){
                angular.copy(vm.selectedTeams, vm.swiss.participants);
                console.log("vm.selectedTeams: " + vm.selectedTeams);
                console.log("vm.swiss.participants: " + vm.swiss.participants);
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
        vm.preparedSettings = vm.swiss.setSettings 
                || {id: null, maxScore: null, leadByPoints: null, minReachedPoints: null};
        
        function initSetSettingsChosen() {
            if (vm.swiss.id !== null && vm.swiss.setSettings !== null) {
                if(vm.swiss.setSettings.leadByPoints !== null 
                    || vm.swiss.setSettings.minReachedScore !== null){
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
                    vm.swiss.setSettings = vm.preparedSettings;
                    console.log(vm.swiss.setSettings);
            }
        }
        /* END - setSettings stuff */

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            
            selectParticipants();
            resolveSetSettings();
            
            if (vm.swiss.id !== null) {
                Swiss.update(vm.swiss, onSaveSuccess, onSaveError);
            } else {
                Swiss.save(vm.swiss, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:swissUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
