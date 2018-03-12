(function () {
    'use strict';

    angular
            .module('tournamentControlApp')
            .controller('EliminationDialogController', EliminationDialogController);

    EliminationDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Elimination', 'Participant', 'filterFilter', 'My'];

    function EliminationDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Elimination, Participant, filterFilter, My) {
        var vm = this;

        vm.elimination = entity;
        
        vm.clear = clear;
        vm.save = save;
        
        vm.getName = My.getParticipantName;
        vm.printRivals = My.printRivals;
        vm.isBye = My.isBye;
        vm.myIterator = My.iterator;
        
        
    /* *** initiating default values *** */
        vm.elimination.bronzeMatch = vm.elimination.bronzeMatch || false;
        vm.elimination.type = vm.elimination.type || "SINGLE";
        vm.elimination.tiesAllowed = false;
        vm.elimination.pointsForWinning = vm.elimination.pointsForWinning || 1;
        vm.elimination.pointsForLosing = vm.elimination.pointsForLosing || 0;
        vm.elimination.setsToWin = vm.elimination.setsToWin || 1;
    /* *** END: initiating default values *** */
        
    /**** participants stuff ****/
        vm.participants = Participant.query();
        vm.participantsTouched = false; //not necessary in here
        
        vm.elimination.participants = vm.elimination.participants || [];
        vm.selectedPlayers = filterFilter(vm.elimination.participants, {team : null, bye:false});
        vm.selectedTeams = filterFilter(vm.elimination.participants, {player : null, bye:false});
        $scope.chosen = 1;
        
        function selectParticipants() {
            if($scope.chosen === 1){
                angular.copy(vm.selectedPlayers, vm.elimination.participants);
            }
            if($scope.chosen === 2){
                angular.copy(vm.selectedTeams, vm.elimination.participants);
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
        
        
        var addSetToSet= function (addInto, set){
            if(set){
                set.forEach(function (item) {
                    addInto.add(item);
                });
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
        
        /**
         * Creates a set of all players from all selected teams.
         * Used for checking which teams are valid (don't have common members)
         * 
         * @returns {undefined}
         */
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
            vm.prepareTeamSeeding();
        };
        
        vm.onPlayerClick = function () {
            vm.participantsTouched = true;
            vm.playersChanged = true;
            vm.preparePlayerSeeding();
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
                vm.selectedPlayers = filterFilter(vm.participants, {team : null, bye: false});
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
        
    /* *** participant seeding stuff *** */
        getSeeding();

        vm.player_seeding = [];
        vm.team_seeding = [];

        function getSeeding() {
            if(vm.elimination.id){
                Elimination.getSeeding({id: vm.elimination.id}, onGetSeedingSuccess, onGetSeedingError);
                function onGetSeedingSuccess(seeding) {
                    vm.player_seeding = filterFilter(seeding, {team : null});
                    vm.team_seeding = filterFilter(seeding, {player : null});
                }
                function onGetSeedingError(error) {
                    console.log("getSeeding() - error occured: ", error.data.message);
                }
            }
        }
        
        function init_seedRandomly_radioBTN() {
            if(vm.elimination.id === null){ //new tournament
                return true;
            }
            return false; //editing old tournament
        }
        $scope.seedRandomly = init_seedRandomly_radioBTN();
        
        vm.coordinates = [];
        
        vm.playersChanged = false;
        vm.teamsChanged = false;
        
        function nodeHTML(id,A,B) {
            return  '<div id="'+id+'" class="tree-node-table not_a_first_round">'+
                        '<div name="A" class="tree-node-table-A">'+vm.getName(A)+'</div>'+
                        '<div name="B" class="tree-node-table-B">'+vm.getName(B)+'</div>'+
                     '</div>';
        }
        
        function under_dragNodeHTML(i, N, seeding){
            var aName = vm.getName(seeding[i]);
            var bName = vm.getName(seeding[N - 1 - i]);
            return  '<div id="'+i+'" class="tree-node-table .this-is-under">'+
                        '<div name="A" class="tree-node-table-A">'+aName+'</div>'+
                        '<div name="B" class="tree-node-table-B">'+bName+'</div>'+
                     '</div>';
        }
        
        function parentIndex(i, N) {
            if(i===N-2) return -1;
            return Math.floor((i + N - 2)/2)+1;
        }
        
        function player_tree(N) {
            var config = {container: '#player_tree', rootOrientation: "EAST", nodeAlign: "BOTTOM",
                connectors: {type: 'step'}};
            if(N<=2){
                var only = {innerHTML: under_dragNodeHTML(0, N, vm.player_seeding)};
                var nodes = [config, only];
                return nodes;
            }
            
            var numberOfNodes = (N-1);
            var numberOfSelectableNodes = N/2;
            
            var rootID = numberOfNodes-1;
            var root = {innerHTML: nodeHTML(rootID, '', '')};
            var nodes = [];
            
            nodes[rootID]=root;
            for (var i = rootID-1 ; i >= 0; i--) {
                var node;
                if(i>=numberOfSelectableNodes){//parents
                    node = {parent: nodes[parentIndex(i, N)], innerHTML: nodeHTML(i, null, null)};
                }else{//selectable
                    node = {parent: nodes[parentIndex(i, N)], innerHTML: under_dragNodeHTML(i, N, vm.player_seeding)};
                }
                nodes[i]=node;
            }
            nodes.push(config);
            nodes.reverse();
            return nodes;
        }
        
        function team_tree(N) {
            var config = {container: '#team_tree', rootOrientation: "EAST", nodeAlign: "BOTTOM",
                connectors: {type: 'step'}};
            if(N<=2){
                var only = {innerHTML: under_dragNodeHTML(0, N, vm.team_seeding)};
                var nodes = [config, only];
                return nodes;
            }
            
            var numberOfNodes = (N-1);
            var numberOfSelectableNodes = N/2;
            
            var rootID = numberOfNodes-1;
            var root = {innerHTML: nodeHTML(rootID, '', '')};
            var nodes = [];
            
            nodes[rootID]=root;
            for (var i = rootID-1 ; i >= 0; i--) {
                var node;
                if(i>=numberOfSelectableNodes){//parents
                    node = {parent: nodes[parentIndex(i, N)], innerHTML: nodeHTML(i, null, null)};
                }else{//selectable
                    node = {parent: nodes[parentIndex(i, N)], innerHTML: under_dragNodeHTML(i, N, vm.team_seeding)};
                }
                nodes[i]=node;
            }
            nodes.push(config);
            nodes.reverse();
            return nodes;
        }
        
        vm.prepareSeeding = function () {
            if($scope.chosen === 1){
                vm.preparePlayerSeeding();
            }
            if($scope.chosen === 2){
                vm.prepareTeamSeeding();
            }
        };
        
        vm.preparePlayerSeeding = function () {
            if($scope.seedRandomly){
                return;
            }
            if(!vm.selectedPlayers || vm.selectedPlayers.length < 3){
                return;
            }
            var n = vm.selectedPlayers.length;
            
            vm.N = My.getN(n);
            var byesNum = vm.N - n;
            if(vm.playersChanged){
                vm.player_seeding = new Array();
                for (var i = 0; i < vm.N; i++) {
                    vm.player_seeding.push(null);
                }
            }
            if(byesNum > 0){
                var BYE = Participant.getBye();
                for (var i = 0; i < byesNum; i++) {
                    vm.player_seeding[vm.N-1-i] = BYE;
                }
            }
            
            new Treant(player_tree(vm.N));
            
            getPlayerCoordinatesAndSize(vm.N);
        };
        
        vm.prepareTeamSeeding = function () {
            if($scope.seedRandomly){
                return;
            }
            if(!vm.selectedTeams || vm.selectedTeams.length < 3){
                return;
            }
            var n = vm.selectedTeams.length;
            
            vm.N = My.getN(n);
            var byesNum = vm.N - n;
            if(vm.teamsChanged){
                vm.team_seeding = new Array();
                for (var i = 0; i < vm.N; i++) {
                    vm.team_seeding.push(null);
                }
            }
            if(byesNum > 0){
                var BYE = Participant.getBye();
                for (var i = 0; i < byesNum; i++) {
                    vm.team_seeding[vm.N-1-i] = BYE;
                }
            }
            
            new Treant(team_tree(vm.N));
            
            getTeamCoordinatesAndSize(vm.N);
        };
        
        /**
         * Run prepareSeeding() once on load in case user is editing existing tournament
         * and thus seedRandomly is initialised to false to preserve old seeding. 
         */
        angular.element(document).ready(function () {
            vm.prepareSeeding();
        });
        
        function getElementCoordinates(i){
            var el = document.getElementById(i);
            var parent = angular.element(el).parent();
            var node = {top: parent.css("top"), left: parent.css("left")};
            vm.coordinates[i]=node;
        }
        
        function getPlayerCoordinatesAndSize(N) {
            vm.coordinates = Array.apply(null, Array(N/2)).map(function () {});
            for (var i = 0; i < N/2; i++) {
                if(!vm.coordinates[i]){
                    getElementCoordinates(i);
                }
            }
            
            vm.treeSize = {};
            var t = angular.element(document.getElementById("player_tree"));
            vm.treeSize.width = t.css("width");
            vm.treeSize.height = t.css("height"); 
        }
        
        function getTeamCoordinatesAndSize(N) {
            vm.coordinates = Array.apply(null, Array(N/2)).map(function () {});
            for (var i = 0; i < N/2; i++) {
                if(!vm.coordinates[i]){
                    getElementCoordinates(i);
                }
            }
            
            vm.treeSize = {};
            var t = angular.element(document.getElementById("team_tree"));
            vm.treeSize.width = t.css("width");
            vm.treeSize.height = t.css("height"); 
        }
        
        vm.contains = My.containsElemWithId;
        
        vm.onPlayerSelect = function (oldSeeding, newRival, i) {
            var oldRival = oldSeeding[i];
            //if set to an already chosen value, switch it for its old
            if(newRival && vm.contains(oldSeeding, newRival)){
                //var oldIndexOfNewRival = oldSeeding.findIndex(item => item && item.id === newRival.id);
                var oldIndexOfNewRival = My.getIndexById(oldSeeding, newRival);
                //swap
                vm.player_seeding[oldIndexOfNewRival] = oldRival;
            }
        };
        
        vm.onTeamSelect = function (oldSeeding, newRival, i) {
            var oldRival = oldSeeding[i];
            //if set to an already chosen value, switch it for its old
            if(newRival && vm.contains(oldSeeding, newRival)){
                //var oldIndexOfNewRival = oldSeeding.findIndex(item => item && item.id === newRival.id);
                var oldIndexOfNewRival = My.getIndexById(oldSeeding, newRival);
                //swap
                vm.team_seeding[oldIndexOfNewRival] = oldRival;
            }
        };
        
        vm.invalidSeeding = function () {
            if(!$scope.seedRandomly){//custom
                var seeding = vm.player_seeding;
                var selectedParticipantsLength = vm.selectedPlayers.length;
                if($scope.chosen===2){//teams
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
        
        vm.getSelectedParticipants = function () {
            if($scope.chosen === 1) return vm.selectedPlayers;
            if($scope.chosen === 2) return vm.selectedTeams;
        };
    /* *** participant seeding stuff END *** */
        
    /* setSettings stuff */
        vm.setSettingsChosen = initSetSettingsChosen();
        vm.preparedSettings = vm.elimination.setSettings 
                || {id: null, maxScore: null, leadByPoints: null, minReachedPoints: null};
        
        function initSetSettingsChosen() {
            if (vm.elimination.id !== null && vm.elimination.setSettings !== null) {
                if(vm.elimination.setSettings.leadByPoints !== null 
                    || vm.elimination.setSettings.minReachedScore !== null){
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
                    vm.elimination.setSettings = vm.preparedSettings;
            }
        }
    /* END - setSettings stuff */
        
        $('.collapse').collapse();
        
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
            
            vm.eliminationDTO = {elimination: vm.elimination};
            
            if($scope.seedRandomly || vm.elimination.participants.length < 3){
                vm.eliminationDTO.seeding = null;
            }else {
                if($scope.chosen === 1) vm.eliminationDTO.seeding = vm.player_seeding;
                if($scope.chosen === 2) vm.eliminationDTO.seeding = vm.team_seeding;
            }

            if (vm.elimination.id !== null) {
                Elimination.update(vm.eliminationDTO, onSaveSuccess, onSaveError);
            } else {
                Elimination.save(vm.eliminationDTO, onSaveSuccess, onSaveError);
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
        
        vm.logSeeding= function () {
            console.log('vm.player_seeding= ', vm.player_seeding );
            console.log('vm.team_seeding= ', vm.team_seeding );
        };

    }
})();
