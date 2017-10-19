(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('SwissDialogController', SwissDialogController);

    SwissDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Swiss', 'filterFilter', 'SetSettings', 'Participant', 'My'];

    function SwissDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Swiss, filterFilter, SetSettings, Participant, My) {
        var vm = this;

        vm.swiss = entity;
        vm.clear = clear;
        vm.save = save;
        
        vm.printRivals = My.printRivals;
        
        /* default values*/
        vm.swiss.pointsForLosing = vm.swiss.pointsForLosing || 0;
        vm.swiss.pointsForTie = vm.swiss.pointsForTie || 0.5;
        vm.swiss.pointsForWinning = vm.swiss.pointsForWinning || 1;
        vm.swiss.tiesAllowed = vm.swiss.tiesAllowed || true;
        vm.swiss.playingFields = vm.swiss.playingFields || 1;
        vm.swiss.setsToWin = vm.swiss.setsToWin || 1;
        vm.swiss.color = vm.swiss.color || false;
        /* end default values*/
        
        
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
        
        /* seeding stuff */
        vm.selectedParticipants = angular.copy(vm.swiss.participants);
        function init_seeding() {
            if(vm.swiss.id){
                //old seeding 
                var oldSeeding = Swiss.getSeeding({id: vm.swiss.id});
                if(oldSeeding){
                    return oldSeeding;
                }
            }
            return [];
        }
        vm.seeding = init_seeding();
        
        function init_seedRandomly_radioBTN() {
            if(vm.swiss.id === null){ //new tournament
                return true;
            }
            return false; //editing old tournament
        }
        $scope.seedRandomly = init_seedRandomly_radioBTN();
        
        vm.changed = false;
        vm.iterator = My.iterator;
        vm.isBye = My.isBye;
        
        vm.update_SelectedParticipants = function () {
            if($scope.chosen === 1){
                angular.copy(vm.selectedPlayers, vm.selectedParticipants);
            }
            if($scope.chosen === 2){
                angular.copy(vm.selectedTeams, vm.selectedParticipants);
            }
        };
        
        vm.bye = Participant.getBye();
        
        vm.prepareSeeding = function (doesNotNeedEmptying) {
            //init empty seeding[] with proper number of byes from selected participants
            if(!doesNotNeedEmptying || vm.changed){
                var n = vm.selectedParticipants.length;
                vm.seeding = new Array();
            }
            
            //n===1 not generated 
            if(vm.selectedParticipants && n === 2){
                //just fill the seeding with seleted participants
                for (var i = 0; i < n; i++) {
                    vm.seeding[i]=vm.selectedParticipants[i];
                }
                return;
            }   
            
            //init nulls
            for (var i = 0; i < n; i++) {
                vm.seeding.push(null);
            }
            
            var byeNum = n % 2;
            //max 1 bye
            if(byeNum > 0){
                vm.seeding.push(vm.bye);
            }
            vm.changed = false;
            console.log("vm.prepareSeeding(), seeding:", vm.seeding);
            return;
        };
        
        vm.updateSelectedParticipants_player = function () {
            angular.copy(vm.selectedPlayers, vm.selectedParticipants);
            vm.changed = true;
            if(!$scope.seedRandomly){
                vm.prepareSeeding();
            }
            
        };
        
        vm.updateSelectedParticipants_team = function () {
            angular.copy(vm.selectedTeams, vm.selectedParticipants);
            vm.changed = true;
            if(!$scope.seedRandomly){
                vm.prepareSeeding();
            }
        };
        
        vm.pairsNumber = function () {
            var n = vm.selectedParticipants.length/2;
            return Math.ceil(n);
        };
        
        vm.getN = function () {
            return vm.selectedParticipants.length + (vm.selectedParticipants.length%2);
        };
        
        vm.contains = My.containsElemWithId;
        
        vm.onParticipantSelect = function (oldSeeding, newRival, i) {
            var oldRival = oldSeeding[i];
            //if set to an already chosen value, switch it for its old
            if(newRival && vm.contains(oldSeeding, newRival)){
                var oldIndexOfNewRival = oldSeeding.findIndex(item => item && item.id === newRival.id);
                //swap
                vm.seeding[oldIndexOfNewRival] = oldRival;
            }
        };
        
        vm.invalidSeeding = function () {
            if(!$scope.seedRandomly){//custom
                if(vm.selectedParticipants.length<2){
                    return true;
                }
                if(vm.seeding){
                    return vm.contains(vm.seeding, null);
                }
                //seeding is null > invalid
                return true;
            }
            //seeding is random > by computer so dont worry about it 
            return false; 
        };
        
        vm.getSeedingOptionName = function (participant) {
            return My.getSeedingOptionName(vm.seeding,participant);
        };
        
        /* END - seeding stuff */
        
        /** Playing Fields Validation **/
        vm.participantsTouched = false;
        
        vm.maxPlayingFields = function () {
            if($scope.chosen===1){//players
                return Math.floor(vm.selectedPlayers.length/2);
            }
            else {//teams
                return Math.floor(vm.selectedTeams.length/2);
            }
        };
        
        vm.playingFieldsInvalid = function () {
            return vm.participantsTouched && vm.swiss.playingFields > vm.maxPlayingFields();
        };
        /** end - Playing Fields Validation **/

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
            
            vm.swissDTO = {swiss: vm.swiss};
            if($scope.seedRandomly || vm.selectedParticipants.length < 2){
                vm.swissDTO.seeding = null;
            }else {
                vm.swissDTO.seeding = vm.seeding;
            }
            
            if (vm.swiss.id !== null) {
                Swiss.update(vm.swissDTO, onSaveSuccess, onSaveError);
            } else {
                Swiss.save(vm.swissDTO, onSaveSuccess, onSaveError);
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
        
        vm.getRoundNum = function () {
            if(vm.selectedParticipants.length === 0)return 0;
            return Math.ceil(Math.log2(vm.selectedParticipants.length));
        };
    }
})();
