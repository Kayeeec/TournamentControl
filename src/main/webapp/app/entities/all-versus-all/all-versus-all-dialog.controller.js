(function () {
    'use strict';

    angular
            .module('tournamentControlApp')
            .controller('AllVersusAllDialogController', AllVersusAllDialogController);

    AllVersusAllDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 
        'entity', 'AllVersusAll', 'Participant', 'filterFilter', 'SetSettings', 'My'];

    function AllVersusAllDialogController($timeout, $scope, $stateParams, $uibModalInstance, 
    entity, AllVersusAll, Participant, filterFilter, SetSettings, My) {
        var vm = this;
        
        vm.allVersusAll = entity;
        vm.clear = clear;
        vm.save = save;
        var contains = My.containsElemWithId; //contains(array,elem)
        
//        initiating default values 
        vm.allVersusAll.tiesAllowed =  vm.allVersusAll.tiesAllowed || true ;
        vm.allVersusAll.numberOfMutualMatches = vm.allVersusAll.numberOfMutualMatches || 1;
        
        /**** participants stuff ****/
        vm.participants = Participant.query();
        vm.participantsTouched = false;
        
        vm.allVersusAll.participants = vm.allVersusAll.participants || [];
        vm.selectedPlayers = filterFilter(vm.allVersusAll.participants, {team : null});
        vm.selectedTeams = filterFilter(vm.allVersusAll.participants, {player : null});
        $scope.chosen = 1;
        
        function selectParticipants() {
            if($scope.chosen === 1){
                angular.copy(vm.selectedPlayers, vm.allVersusAll.participants);
            }
            if($scope.chosen === 2){
                angular.copy(vm.selectedTeams, vm.allVersusAll.participants);
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
        
        /* Set implementation */
        Set.prototype.addSet = function (set) {
            if(set){
                for (let item of set){
                    this.add(item);
                }
            }
        };
        /* end set implementation */ 

        
        vm.getTeamCSS_onValidity = function (participant) {
            if(vm.teamIsInvalid(participant)){
                return "text text-muted";
            }
            return "";
        };
        
        vm.teamPlayers = init_teamPlayers();
        
        function addTeamMembers(set, participant) {
            for (var i = 0; i < participant.team.members.length; i++) {
                set.add(participant.team.members[i].id);
            }
        }
        
        function init_teamPlayers() {
            var result = new Set();
            for (var i = 0; i < vm.selectedTeams.length; i++) {
                addTeamMembers(result, vm.selectedTeams[i]);
            }
            return result;
        }
        
        function computeTeamPlayers() {
            vm.teamPlayers.clear();
            for (var i = 0; i < vm.selectedTeams.length; i++) {
                addTeamMembers(vm.teamPlayers, vm.selectedTeams[i]);
            }
        }
        
        vm.onTeamClick = function (team) {
            vm.participantsTouched = true;
            computeTeamPlayers();
        };
        
        vm.onPlayerClick = function () {
            vm.participantsTouched = true;
        };
       
        vm.teamIsInvalid= function(participant){
          if(!contains(vm.selectedTeams, participant)){
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
            return vm.participantsTouched && vm.allVersusAll.playingFields > vm.maxPlayingFields();
        };
        /** end - Playing Fields Validation **/
        
        
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
            }
        }
        /* END - setSettings stuff */

        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });
        
        function clear() {
            $uibModalInstance.dismiss('cancel');
        }
        
        function save() {
            
            vm.isSaving = true;
            
            selectParticipants();
            resolveSetSettings();
            
            
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
    }
})();
