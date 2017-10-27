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
        vm.contains = My.containsElemWithId;
        
        /* default values*/
        vm.swiss.pointsForLosing = vm.swiss.pointsForLosing || 0;
        vm.swiss.pointsForTie = vm.swiss.pointsForTie || 0.5;
        vm.swiss.pointsForWinning = vm.swiss.pointsForWinning || 1;
        vm.swiss.tiesAllowed = vm.swiss.tiesAllowed || true;
        vm.swiss.playingFields = vm.swiss.playingFields || 1;
        vm.swiss.setsToWin = vm.swiss.setsToWin || 1;
        vm.swiss.color = vm.swiss.color || false;
        /* end default values*/


        /**** participants stuff ****/
        vm.participants = Participant.query();
        vm.participantsTouched = false;
        
        vm.swiss.participants = vm.swiss.participants || [];
        vm.selectedPlayers = filterFilter(vm.swiss.participants, {team : null, bye:false});
        vm.selectedTeams = filterFilter(vm.swiss.participants, {player : null, bye:false});
        $scope.chosen = 1;
        
        function selectParticipants() {
            if($scope.chosen === 1){
                angular.copy(vm.selectedPlayers, vm.swiss.participants);
            }
            if($scope.chosen === 2){
                angular.copy(vm.selectedTeams, vm.swiss.participants);
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
        
        Set.prototype.addSet = function (set) {
            if(set){
                for (let item of set){
                    this.add(item);
                }
            }
        };
        
        vm.getTeamCSS_onValidity = function (participant) {
            if(vm.teamIsInvalid(participant)){
                return "text text-muted";
            }
            return "";
        };
        
        vm.teamPlayers = new Set();
        
        function addTeamMembers(set, participant) {
            if(!participant || !participant.team || participant.team.members.length === 0){
                return;
            } 
            for (var i = 0; i < participant.team.members.length; i++) {
                set.add(participant.team.members[i].id);
            }
        }
        
        function computeTeamPlayers() {
            vm.teamPlayers.clear();
            for (var i = 0; i < vm.selectedTeams.length; i++) {
                addTeamMembers(vm.teamPlayers, vm.selectedTeams[i]);
            }
        }
        
        vm.onTeamClick = function () {
            vm.participantsTouched = true;
            computeTeamPlayers();
            vm.teamsChanged = true;
            vm.updateSelectedParticipants_team();
            
        };
        
        vm.onPlayerClick = function () {            
            vm.participantsTouched = true;
            vm.playersChanged = true;
            vm.updateSelectedParticipants_player();
        };
       
        vm.teamIsInvalid= function(participant){ 
          if(!vm.contains(vm.selectedTeams, participant)){
            var both = new Set();
            both.addSet(vm.teamPlayers);
            addTeamMembers(both, participant);
            return vm.teamPlayers.size + participant.team.members.length > both.size; 
          }
          return false;
        };
        
        $('#myTab a[href="#players"]').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });
        $('#myTab a[href="#teams"]').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });
        
        vm.selectAll = function () {
            if($scope.chosen===1){
                vm.selectedPlayers = filterFilter(vm.participants, {team : null, bye:false});
                vm.onPlayerClick();
            }
            //select all on teams disabled because of conflicting teams
            
        };        
        
        vm.deselectAll = function () {
            if($scope.chosen===1){
                vm.selectedPlayers = [];
                vm.onPlayerClick();
            }else{
                vm.selectedTeams = [];
                vm.onTeamClick();
            }
        };
        
        /**** end Participant stuff ****/
        
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
            return vm.participantsTouched && vm.swiss.playingFields > vm.maxPlayingFields();
        };
        /** end - Playing Fields Validation **/
        
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
            }
        }
        /* END - setSettings stuff */
        
        /* seeding stuff */
        
        getSeeding();
        
        vm.player_seeding = [];
        vm.team_seeding = [];
        
        function getSeeding() {
            if(vm.swiss.id){
                Swiss.getSeeding({id: vm.swiss.id}, onGetSeedingSuccess, onGetSeedingError);
                function onGetSeedingSuccess(seeding) {
                    vm.player_seeding = filterFilter(seeding, {team : null});
                    vm.team_seeding = filterFilter(seeding, {player : null});
                }
                function onGetSeedingError(error) {
                    console.log('getSeeding() - error occured: ', error.data.message);
                }
            }
        }
        
        function init_seedRandomly_radioBTN() {
            if(vm.swiss.id === null){ //new tournament
                return true;
            }
            return false; //editing old tournament
        }
        $scope.seedRandomly = init_seedRandomly_radioBTN();
        
        vm.playersChanged = false;
        vm.teamsChanged = false;
        
        vm.iterator = My.iterator;
        vm.isBye = My.isBye; 
        vm.bye = Participant.getBye();
        
        vm.prepareSeeding=function (doesNotNeedEmptying) {
            if($scope.chosen === 1){
                vm.preparePlayerSeeding(doesNotNeedEmptying);
            }else{
                vm.prepareTeamSeeding(doesNotNeedEmptying);
            }
        };
        
        vm.preparePlayerSeeding = function (doesNotNeedEmptying) {
            //init empty seeding[] with proper number of byes from selected participants
            if(!doesNotNeedEmptying || vm.playersChanged){
                var n = vm.selectedPlayers.length;
                vm.player_seeding = new Array();
            }
            
            //n===1 not generated 
            //n===2 filled with chosen players
            if(vm.selectedPlayers && n === 2){
                //just fill the seeding with selected participants
                for (var i = 0; i < n; i++) {
                    vm.player_seeding[i]=vm.selectedPlayers[i];
                }
                return;
            }   
            
            //n > 2 initialized with nulls
            for (var i = 0; i < n; i++) {
                vm.player_seeding.push(null);
            }
            
            //max 1 bye
            if(n % 2 > 0){
                vm.player_seeding.push(vm.bye);
            }
            vm.playersChanged = false;
            return;
        };
        
        vm.prepareTeamSeeding = function (doesNotNeedEmptying) {
            //init empty seeding[] with proper number of byes from selected participants
            if(!doesNotNeedEmptying || vm.teamsChanged){
                var n = vm.selectedTeams.length;
                vm.team_seeding = new Array();
            }
            
            //n===1 not generated 
            //n===2 filled with chosen teams
            if(vm.selectedTeams && n === 2){
                //just fill the seeding with selected participants
                for (var i = 0; i < n; i++) {
                    vm.team_seeding[i]=vm.selectedTeams[i];
                }
                return;
            }   
            
            //n > 2 initialized with nulls
            for (var i = 0; i < n; i++) {
                vm.team_seeding.push(null);
            }
            
            //max 1 bye
            if(n % 2 > 0){
                vm.team_seeding.push(vm.bye);
            }
            vm.teamsChanged = false;
            return;
        };
        
        vm.updateSelectedParticipants_player = function (doesNotNeedEmptying) {
            if(!$scope.seedRandomly){
                vm.preparePlayerSeeding(doesNotNeedEmptying);
            }
        };
        
        vm.updateSelectedParticipants_team = function (doesNotNeedEmptying) {
            if(!$scope.seedRandomly){
                vm.prepareTeamSeeding(doesNotNeedEmptying);
            }
        };
        
        vm.pairsNumber = function () {
            if($scope.chosen === 1){
                return Math.ceil(vm.selectedPlayers.length/2);
            }
            return Math.ceil(vm.selectedTeams.length/2);
        };
        
        vm.getN = function () {
            if($scope.chosen === 1){
                return vm.selectedPlayers.length + (vm.selectedPlayers.length%2);
            }
            return vm.selectedTeams.length + (vm.selectedTeams.length%2);
        };
        
        vm.getSelectedParticipants = function () {
            if($scope.chosen === 1){
                return vm.selectedPlayers;
            }
            return vm.selectedTeams;
        };
        
        vm.onPlayerSelect = function (oldSeeding, newRival, i) {
            var oldRival = oldSeeding[i];
            //if set to an already chosen value, switch it for its old
            if(newRival && vm.contains(oldSeeding, newRival)){
                var oldIndexOfNewRival = oldSeeding.findIndex(item => item && item.id === newRival.id);
                //swap
                vm.player_seeding[oldIndexOfNewRival] = oldRival;
            }
        };
        
        vm.onTeamSelect = function (oldSeeding, newRival, i) {
            var oldRival = oldSeeding[i];
            //if set to an already chosen value, switch it for its old
            if(newRival && vm.contains(oldSeeding, newRival)){
                var oldIndexOfNewRival = oldSeeding.findIndex(item => item && item.id === newRival.id);
                //swap
                vm.team_seeding[oldIndexOfNewRival] = oldRival;
            }
        };
        
        vm.invalidSeeding = function () {
            if(!$scope.seedRandomly){//custom
                var seeding = vm.player_seeding;
                var selectedParticipantsLength = vm.selectedPlayers.length;
                if($scope.chosen===2){//players
                    seeding = vm.team_seeding;
                    selectedParticipantsLength = vm.selectedTeams.length;
                }
                
                if(selectedParticipantsLength < 2) return true;
                if(seeding)return vm.contains(seeding,null);
                return true;//seeding is null > invalid ??????   
            }
            //seeding is random > by computer so dont worry about it 
            return false; 
        };
        
        vm.getSeedingOptionName = function (participant) {
            if($scope.chosen === 1){ return My.getSeedingOptionName(vm.player_seeding, participant);}
            return My.getSeedingOptionName(vm.team_seeding, participant);
        };
        
        vm.getRoundNum = function () {
            if(vm.getSelectedParticipants.length === 0)return 0;
            return Math.ceil(Math.log2(vm.getSelectedParticipants.length));
        };
        
        /* END - seeding stuff */
        
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
            if($scope.seedRandomly || vm.swiss.participants.length < 2){
                vm.swissDTO.seeding = null;
            }else {
                if($scope.chosen === 1){
                    vm.swissDTO.seeding = vm.player_seeding;
                }
                if($scope.chosen === 2){
                    vm.swissDTO.seeding = vm.team_seeding;
                }
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
        
        
    }
})();
