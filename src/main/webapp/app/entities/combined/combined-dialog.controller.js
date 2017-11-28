(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('CombinedDialogController', CombinedDialogController)
        .filter('fromMap', function() {
          return function(input) {
            var out = {};
//            input.forEach((v, k) => out[k] = v); //gulp has problems with '=>'
            input.forEach(function (v, k) {
                out[k] = v;
            });
            return out;
          };});

    CombinedDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Combined', 'Participant', 'Tournament', 'User', 'filterFilter', 'My'];

    function CombinedDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Combined, Participant, Tournament, User, filterFilter, My) {
        var vm = this;

        vm.combined = entity;
        vm.clear = clear;
        vm.save = save;
        vm.contains = My.containsElemWithId;
        vm.printListOfParticipants = My.printRivals;


        /* *** default values *** */
        vm.combined.inGroupTournamentType = vm.combined.inGroupTournamentType || "ALL_VERSUS_ALL";
        vm.combined.playoffType = vm.combined.playoffType || "ELIMINATION";
        vm.combined.numberOfWinnersToPlayoff = vm.combined.numberOfWinnersToPlayoff || 1;
        vm.combined.numberOfGroups = vm.combined.numberOfGroups || 1;
        /* *** end - default values *** */

        /* *** all participants stuff *** */
        vm.participants = Participant.query();
        vm.participantsTouched = false;

        vm.combined.participants = vm.combined.participants || [];
        vm.selectedPlayers = filterFilter(vm.combined.participants, {team : null, bye:false});
        vm.selectedTeams = filterFilter(vm.combined.participants, {player : null, bye:false});
        $scope.chosen = init_chosen();

        function init_chosen() {
            if(!vm.combined.allParticipants
                    || vm.combined.allParticipants.length === 0
                    || vm.combined.allParticipants[0].team === null){
                return 1;
            }
            return 2;
        };

        /* ***
         *  used in save() to select final list of participants
         *  *** */
        function selectParticipants() {
            if($scope.chosen === 1){
                angular.copy(vm.selectedPlayers, vm.combined.allParticipants);
            }
            if($scope.chosen === 2){
                angular.copy(vm.selectedTeams, vm.combined.allParticipants);
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

        vm.onTeamClick = function (participant) {
            vm.participantsTouched = true;
            computeTeamPlayers();
            vm.teamsChanged = true;
            
            updateTeamsToChoose(participant);
            
            //if removed - participant needs to be removed from all groups
            if(participant && vm.getIndexById(vm.selectedTeams, participant) === -1){
                //was deselected
                onTeamDeselect(participant);
            }

        };

        vm.onPlayerClick = function (participant) {
            vm.participantsTouched = true;
            vm.playersChanged = true;
            
            updatePlayersToChoose(participant);
            
            //participant needs to be removed from all groups on deselect
            if(participant && vm.getIndexById(vm.selectedPlayers, participant) === -1){
                //was deselected
                onPlayerDeselect(participant);
            }
        };

        var addSetToSet= function (addInto, set){
            if(set){
                set.forEach(function (item) {
                    addInto.add(item);
                });
            }
        };

        vm.teamIsInvalid= function(participant){
          if(!vm.contains(vm.selectedTeams, participant)){
            var both = new Set();
            addSetToSet(both, vm.teamPlayers);
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
                vm.onPlayerClick(null);
            }
            //select all on teams disabled because of conflicting teams

        };

        vm.deselectAll = function () {
            if($scope.chosen===1){
                vm.selectedPlayers = [];
                vm.onPlayerClick(null);
                emptyOutPlayerGroups();
            }else{
                vm.selectedTeams = [];
                vm.onTeamClick();
                emptyOutTeamGroups();
            }
        };
        /* *** end - all participants stuff *** */

        /* *** participant in group assignment stuff *** */
        vm.assign_to_group_manually = false;
        vm.groupPlayerObject = {};
        vm.groupTeamObject = {};
        vm.playersToChoose = [];
        vm.teamsToChoose = [];
        
        vm.getName = My.getParticipantName;
        
        vm.numberOfGroupsChanged = function () {
            if(vm.assign_to_group_manually){
                vm.init_groupParticipantObject();
            }
        };
        function nextChar_upperCase(c) {
            if(c === "Z") return "A";
            return String.fromCharCode(c.charCodeAt(0) + 1);
        }
        function init_GP_Object() {
            console.log("init_groupParticipantObject:");
            var object = {};
            if(!vm.combined.numberOfGroups || vm.combined.numberOfGroups === 0){
                console.log("    no groups");
                return;
            }
            var letter = "@";
            if(!vm.combined.id || vm.participantsTouched || vm.numberOfGroupsChanged){ //new or changed => do over
                console.log("    new init of an object");
                for (var i = 0; i < vm.combined.numberOfGroups; i++) {
                letter = nextChar_upperCase(letter);
                object[letter] = [];
                }
            }else{ //id not null and allParticipants not changed 
                console.log("    loading old object");
                for (var i = 0; i < vm.combined.groups.length; i++) {
                    var group = vm.combined.groups[i];
                    object[group.name] = group.participants;
                }
            }
            return object;
        };
        vm.init_groupParticipantObject = function () {
            vm.playersToChoose = angular.copy(vm.selectedPlayers);
            vm.teamsToChoose = angular.copy(vm.selectedTeams);
            
            if($scope.chosen === 1){
                vm.groupPlayerObject = init_GP_Object();
            }else{
                vm.groupTeamObject = init_GP_Object();
            }
        };
        
        /* *** onPlayerClick in participants stuff *** */
        function updatePlayersToChoose(participant) {
            if(!participant){
                //used for select/deselect all 
                vm.playersToChoose = angular.copy(vm.selectedPlayers);
                return;
            }
            if (vm.getIndexById(vm.selectedPlayers, participant) !== -1 ){
                vm.playersToChoose.push(participant); //is selected, was added
                return;
            }
            //was unselected, remove it 
            removeObjectFromArrayById(vm.playersToChoose, participant);
        }
        function updateTeamsToChoose(participant) {
            if(!participant){
                //used for select/deselect all 
                vm.teamsToChoose = angular.copy(vm.selectedTeams);
                return;
            }
            if (vm.getIndexById(vm.selectedTeams, participant) !== -1 ){
                vm.teamsToChoose.push(participant); //is selected, was added
                return;
            }
            //was unselected, remove it 
            removeObjectFromArrayById(vm.teamsToChoose, participant);
        }
        
        vm.getIndexById = My.getIndexById;
        
        /**
         * 
         * @param {type} array
         * @param {type} object that contains id
         * @returns true if something was removed, false otherwise 
         **/
        var removeObjectFromArrayById = function (array, object) {
            if(!object || !array)return false;
            var index = vm.getIndexById(array, object);
            if(index !== -1){
                array.splice(index, 1);
                return true;
            }
            return false;   
        };
        
        
        /* *** if participant was removed from vm.selectedPlayers, he has to be removed from his group *** */
        function onPlayerDeselect(participant) {               
            for (var group in vm.groupPlayerObject) {
                if(removeObjectFromArrayById(vm.groupPlayerObject[group], participant)){
                    break;
                }
            }
        }function onTeamDeselect(participant) {               
            for (var group in vm.groupTeamObject) {
                if(removeObjectFromArrayById(vm.groupTeamObject[group], participant)){
                    break;
                }
            }
        }
        
        vm.onPlayerUiSelect_Select = function (group, participant) {
            //remove from vm.playersToChoose
            removeObjectFromArrayById(vm.playersToChoose, participant);
            
        };
        vm.onPlayerUiSelect_Remove = function (group, participant) {
            //put back to vm.playersToChoose
            vm.playersToChoose.push(participant);
        };
        
        vm.onTeamUiSelect_Select = function (group, participant) {
            //remove from vm.teamsToChoose
            removeObjectFromArrayById(vm.teamsToChoose, participant);
            
        };
        vm.onTeamUiSelect_Remove = function (group, participant) {
            //put back to vm.playersToChoose
            vm.teamsToChoose.push(participant);
        };
        
        /**
         * empties out all the groups 
         */
        function emptyOutPlayerGroups() {
            for (var group in vm.groupPlayerObject) {
                vm.groupPlayerObject[group] = [];
            }
        }
        function emptyOutTeamGroups() {
            for (var group in vm.groupTeamObject) {
                vm.groupTeamObject[group] = [];
            }
        }
        
       /* *** END participant in group assignment stuff *** */
       
        /* *** seeding stuff *** */
        vm.seed_manually = false;
        
        vm.invalidGroupAssignment = function () {
            if($scope.chosen === 1){
                return !(vm.playersToChoose.length === 0);
            }else{
                return !(vm.teamsToChoose.length === 0);
            }
        };
        
        vm.noParticipantsSelected = function () {
            if($scope.chosen === 1){
                return vm.selectedPlayers.length === 0;
            }else{
                return vm.selectedTeams.length === 0;
            }
        };
        
        
        
        /* *** end - seeding stuff *** */

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.combined.id !== null) {
                Combined.update(vm.combined, onSaveSuccess, onSaveError);
            } else {
                Combined.save(vm.combined, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:combinedUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }
    }
})();
