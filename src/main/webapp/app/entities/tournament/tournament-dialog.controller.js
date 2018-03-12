(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TournamentDialogController', TournamentDialogController);

    TournamentDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', '$q', 'entity', 'Tournament', 'Game', 'User', 'Participant', 'SetSettings'];

    function TournamentDialogController ($timeout, $scope, $stateParams, $uibModalInstance, $q, entity, Tournament, Game, User, Participant, SetSettings) {
        var vm = this;

        vm.tournament = entity;
        vm.clear = clear;
        vm.save = save;
        vm.participants = Participant.query();
        $scope.player = 1;
        
        /* participants stuff */
        vm.tournament.participants = vm.tournament.participants || [];
        vm.selectedPlayers = filterFilter(vm.tournament.participants, {team : null});
        vm.selectedTeams = filterFilter(vm.tournament.participants, {player : null});
        $scope.chosen = 1;
        
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
        vm.preparedSettings = vm.tournament.setSettings 
                || {id: null, maxScore: null, leadByPoints: null, minReachedPoints: null};
        
        function initSetSettingsChosen() {
            if (vm.tournament.id !== null && vm.tournament.setSettings !== null) {
                if(vm.tournament.setSettings.leadByPoints !== null 
                    || vm.tournament.setSettings.minReachedScore !== null){
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
                    vm.tournament.setSettings = vm.preparedSettings;
                    console.log(vm.tournament.setSettings);
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
            if (vm.tournament.id !== null) {
                Tournament.update(vm.tournament, onSaveSuccess, onSaveError);
            } else {
                Tournament.save(vm.tournament, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:tournamentUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.created = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
