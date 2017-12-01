(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('CombinedDialogController', CombinedDialogController);

    CombinedDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Combined', 'Participant', 'Tournament', 'User', 'filterFilter', 'My'];

    function CombinedDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Combined, Participant, Tournament, User, filterFilter, My) {
        var vm = this;

        vm.combined = entity;
        vm.clear = clear;
        vm.save = save;
        vm.contains = My.containsElemWithId;
        vm.printListOfParticipants = My.printRivals;
        vm.myIterator = My.iterator;


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
            vm.initSeeding();
                

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
            vm.initSeeding();
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
                vm.init_groupParticipantObject();
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
        vm.minNumOfGroupMembers = function () {
            return Math.max(vm.combined.numberOfWinnersToPlayoff, 1);
        };
        
        vm.getName = My.getParticipantName;

        vm.groupNumberChanged = true;
        vm.groupAssignmentChanged = false;

        vm.numberOfGroupsChanged = function () {
            vm.groupNumberChanged = true;
            vm.init_groupParticipantObject();
            
        };
        function nextChar_upperCase(c) {
            if(c === "Z") return "A";
            return String.fromCharCode(c.charCodeAt(0) + 1);
        }
        
        function initPlayerGroupingObject(noNeedToEmpty) {
            console.log("init_groupParticipantObject:");
            if (!vm.combined.numberOfGroups || vm.combined.numberOfGroups === 0) {
                console.log("    no groups");
                return;
            }
            if (vm.combined.numberOfGroups === 1) {
                vm.groupPlayerObject = {};
                vm.groupPlayerObject["A"] = angular.copy(vm.selectedPlayers);
                vm.playersToChoose = [];
                console.log("   groupPlayerObject: ", vm.groupPlayerObject);
                return;
            }
            var oldObject = Object.create(vm.groupPlayerObject);
            if (vm.groupNumberChanged) {
                vm.groupPlayerObject = {};
            }
            var letter = "@";
            for (var i = 0; i < vm.combined.numberOfGroups; i++) {
                letter = nextChar_upperCase(letter);
                if (noNeedToEmpty && oldObject[letter]) {
                    vm.groupPlayerObject[letter] = angular.copy(oldObject[letter]);
                } else {
                    vm.groupPlayerObject[letter] = [];
                }
            }
        }
        
        function initTeamGroupingObject(noNeedToEmpty) {
            console.log("init_groupParticipantObject:");
            if (!vm.combined.numberOfGroups || vm.combined.numberOfGroups === 0) {
                console.log("    no groups");
                return;
            }
            if (vm.combined.numberOfGroups === 1) {
                vm.groupTeamObject = {};
                vm.groupTeamObject["A"]=angular.copy(vm.selectedTeams);
                vm.teamsToChoose = [];
                return;

            }
            var oldObject = angular.copy(vm.groupTeamObject);
            if (vm.groupNumberChanged) {
                vm.groupTeamObject = {};
            }
            var letter = "@";
            for (var i = 0; i < vm.combined.numberOfGroups; i++) {
                letter = nextChar_upperCase(letter);
                if (noNeedToEmpty && oldObject[letter]) {
                    vm.groupTeamObject[letter] = angular.copy(oldObject[letter]);
                } else {
                    vm.groupTeamObject[letter] = [];
                }
            }
        }
        
        vm.init_groupParticipantObject = function (noNeedToEmpty) {
            if(vm.assign_to_group_manually){
                if(!noNeedToEmpty){
                    vm.playersToChoose = angular.copy(vm.selectedPlayers);
                    vm.teamsToChoose = angular.copy(vm.selectedTeams);
                }
                
                if($scope.chosen === 1){
                    initPlayerGroupingObject(noNeedToEmpty);
                }else{
                    initTeamGroupingObject(noNeedToEmpty);
                }
                vm.groupNumberChanged = false;

                vm.initSeeding(noNeedToEmpty); //checks vm.seed_manually 
            }
        };
        
        /**
         * get old group object, ran once on page load lkike getSeeding()
         */
        function getOldGroupObject() {
            if(vm.combined.id && vm.combined.groups && vm.combined.groups.length > 0){
                for (var i = 0; i < vm.combined.groups.length; i++) {
                    var groupTournament = vm.combined.groups[i];
                    vm.playerGroupObject[groupTournament.name] = 
                            filterFilter(groupTournament.participants, {team : null});
                    vm.teamGroupObject[groupTournament.name] = 
                            filterFilter(groupTournament.participants, {player : null});
                    vm.playersToChoose = [];
                    vm.teamsToChoose = [];
                }
            }else if (!vm.combined.id) {
                vm.init_groupParticipantObject();
            }

        }
        getOldGroupObject();

        /* *** onPlayerClick in participants stuff *** */
        function updatePlayersToChoose(participant) {
            vm.groupAssignmentChanged = true;
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
            vm.groupAssignmentChanged = true;
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
            vm.groupAssignmentChanged = true;
            vm.groupNumberChanged = false;

        };
        vm.onPlayerUiSelect_Remove = function (group, participant) {
            //put back to vm.playersToChoose
            vm.playersToChoose.push(participant);
            vm.groupAssignmentChanged = true;
            vm.groupNumberChanged = false;
        };

        vm.onTeamUiSelect_Select = function (group, participant) {
            //remove from vm.teamsToChoose
            removeObjectFromArrayById(vm.teamsToChoose, participant);
            vm.groupAssignmentChanged = true;
            vm.groupNumberChanged = false;

        };
        vm.onTeamUiSelect_Remove = function (group, participant) {
            //put back to vm.playersToChoose
            vm.teamsToChoose.push(participant);
            vm.groupAssignmentChanged = true;
            vm.groupNumberChanged = false;
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

        /** *** seeding stuff *** **/
        vm.seed_manually = false;
        vm.playerSeedingObject = {};
        vm.teamSeedingObject = {};
        vm.bye = Participant.getBye();
        vm.iterator = My.iterator;
        vm.isBye = My.isBye;
        
        vm.pairsNumber = function(group){
            console.log("pairs number");
            if($scope.chosen === 1){
                console.log("     group: ", group, ", n: ", vm.playerSeedingObject[group].length, " result: ", Math.ceil(vm.playerSeedingObject[group].length/2));
             return Math.ceil(vm.playerSeedingObject[group].length/2);
            }
            console.log("     group: ", group, ", n: ", vm.teamSeedingObject[group].length, " result: ", Math.ceil(vm.teamSeedingObject[group].length/2));
            return Math.ceil(vm.teamSeedingObject[group].length/2);
        };
        
        function init_player_seeding_object(noNeedToEmpty) {
            console.log("init_player_seeding_object");
            var object = {};
            var old = Object.create(vm.playerSeedingObject);
            var letter = "@";

            //new or changed
            for (var i = 0; i < vm.combined.numberOfGroups; i++) {
                letter = nextChar_upperCase(letter);
                console.log("   ",letter);
                if(noNeedToEmpty && old.hasOwnProperty(letter) && !vm.groupAssignmentChanged){
                    object[letter] = angular.copy(old[letter]);
                }else{
                    object[letter] = [];
                    
                    //fill with proper number of nulls and byes
                    var numberOfByes = getNumberOfByes(vm.groupPlayerObject[letter].length);
                    for (var n = 0; n < vm.groupPlayerObject[letter].length; n++ ){
                        console.log("   pushing null ", n);
                        object[letter].push(null);
                    }
                    if (numberOfByes > 0) {
                        var l = vm.groupPlayerObject[letter].length;
                        for (var b = 0; b < numberOfByes; b++) {
                            console.log("   pushing BYE: ", l+b);
                            object[letter][l+b] = JSON.parse(JSON.stringify(vm.bye));
                        }
                    }
                    
                }
            }
            vm.playerSeedingObject = object;
            console.log("    result - vm.playerSeedingObject ", vm.playerSeedingObject);
        }
        
        function init_team_seeding_object(noNeedToEmpty) {
            console.log("init_team_seeding_object");
            var object = {};
            var old = Object.create(vm.teamSeedingObject);
            var letter = "@";

            //new or changed
            for (var i = 0; i < vm.combined.numberOfGroups; i++) {
                letter = nextChar_upperCase(letter);
                console.log("   ",letter);
                if(noNeedToEmpty && old.hasOwnProperty(letter) && !vm.groupAssignmentChanged){
                    object[letter] = angular.copy(old[letter]);
                }else{
                    object[letter] = [];
                    
                    //fill with proper number of nulls and byes
                    var numberOfByes = getNumberOfByes(vm.groupTeamObject[letter].length);
                    for (var n = 0; n < vm.groupTeamObject[letter].length; n++ ){
                        console.log("   pushing null ", n);
                        object[letter].push(null);
                    }
                    if (numberOfByes > 0) {
                        var l = vm.groupTeamObject[letter].length;
                        for (var b = 0; b < numberOfByes; b++) {
                            console.log("   pushing BYE: ", l+b);
                            object[letter][l+b] = JSON.parse(JSON.stringify(vm.bye));
                        }
                    }
                    
                }
            }
            vm.teamSeedingObject = object;
            console.log("    result - vm.teamSeedingObject ", vm.teamSeedingObject);
        }
        
        /* *** 
         *  noNeedToEmpty = true: 
         *          old seeding remains 
         *  
         *    *** */
        vm.initSeeding = function (noNeedToEmpty) {
            console.log("vm.initSeeding()");
            if(!vm.seed_manually || old_and_unchanged()){
                console.log("    nothing to init - seed manually = ", vm.seed_manually, ", old and unchanged = ", old_and_unchanged());
                return;
            }
            if($scope.chosen === 1){
                //vm.playerSeedingObject = init_seeding_object(vm.groupPlayerObject,vm.playerSeedingObject, noNeedToEmpty);
                init_player_seeding_object(noNeedToEmpty);
                
                if(vm.combined.inGroupTournamentType === 'ELIMINATION'){
                    for (var group in vm.playerSeedingObject) {
                        if(vm.playerSeedingObject.hasOwnProperty(group)){
                            vm.initPlayerTree(group);
                            console.log("  vm.coordinates: ", vm.coordinates);
                        }
                    }
                }
                
            }else{
//                vm.teamSeedingObject = init_seeding_object(vm.groupTeamObject,vm.teamSeedingObject, noNeedToEmpty);
                init_team_seeding_object(noNeedToEmpty);
                
                if(vm.combined.inGroupTournamentType === 'ELIMINATION'){
                    for (var group in vm.teamSeedingObject) {
                        if(vm.teamSeedingObject.hasOwnProperty(group)){
                            vm.initTeamTree(group);
                        }
                    }
                }
            }
            vm.groupAssignmentChanged = false;
        };
        
        function old_and_unchanged() {
            return vm.combined.id && !vm.participantsTouched && !vm.groupNumberChanged && !vm.groupAssignmentChanged;
        };

        function getSeeding() {
            console.log("getSeeding()");
            if (vm.combined.id && vm.combined.groups){
                for (var groupTournament in vm.combined.groups){
                    Tournament.getSeeding({id: groupTournament}, onGetSeedingSuccess, onGetSeedingError);
                    function onGetSeedingSuccess(seeding) {
                        vm.playerSeedingObject[groupTournament.name] = filterFilter(seeding, {team : null});
                        vm.teamSeedingObject[groupTournament.name] = filterFilter(seeding, {player : null});
                    }
                    function onGetSeedingError(error) {
                        console.log('getSeeding() - error occured: ', error.data.message);
                    }
                }
            }
            if(!vm.combined.id){
                vm.initSeeding();
            }
        }getSeeding();

        function getNumberOfByes(n) {
            if(vm.combined.inGroupTournamentType === 'SWISS'){
                return (n % 2);
            }
            if(vm.combined.inGroupTournamentType === 'ELIMINATION'){
                var N = My.getN(n);
                console.log("N: ",N,", n: ",n,", byes: ",N-n);
                return N - n;
            }
        }

        vm.invalidGroupAssignment = function () {
            if($scope.chosen === 1){
                if(vm.playersToChoose.length !== 0){
                    console.log("    cause 1");
                    return true;
                }
                for (var group in vm.groupPlayerObject){
                    if(vm.groupPlayerObject[group].length < vm.minNumOfGroupMembers){
                        console.log("    cause 2");
                        return true;
                    }
                }
                return false;
            }else{
                if(vm.teamsToChoose.length !== 0){
                    console.log("    cause 1");
                    return true;
                }
                for (var group in vm.groupTeamObject){
                    if(vm.groupTeamObject[group].length < vm.minNumOfGroupMembers){
                        console.log("    cause 2");
                        return true;
                    }
                }
                return false;
            }
        };

        vm.noParticipantsSelected = function () {
            if($scope.chosen === 1){
                return vm.selectedPlayers.length === 0;
            }else{
                return vm.selectedTeams.length === 0;
            }
        };

        vm.getRivalIndex = function (group, i) {
            console.log("getRivalIndex");
            if($scope.chosen === 1){
                var seedingLen = vm.playerSeedingObject[group].length;
                console.log("     group: ", group, ", i: ", i, ", seedingLen: ", seedingLen);
                return seedingLen - i - 1;
            }
            var seedingLen = vm.teamSeedingObject[group].length;
            console.log("     group: ", group, ", i: ", i, ", seedingLen: ", seedingLen);
            return seedingLen - i - 1;
        };
        
        vm.tickIfSelected = function (group, participant) {
            if(group && participant){
                if($scope.chosen === 1){
                    if(vm.getIndexById(vm.playerSeedingObject[group], participant)!==-1){
                        //has been seeded
                        return 'glyphicon glyphicon-check';
                    }
                    return 'glyphicon glyphicon-unchecked';
                }else{
                    if(vm.getIndexById(vm.teamSeedingObject[group], participant)!==-1){
                        //has been seeded
                        return 'glyphicon glyphicon-check';
                    }
                    return 'glyphicon glyphicon-unchecked';
                }
            }
        };
        
        vm.onPlayerSelect = function (oldSeeding, newRival, i, group) {
            
            var oldRival = oldSeeding[i];
            //if set to an already chosen value, switch it for its old
            var oldIndexOfNewRival = vm.getIndexById(oldSeeding, newRival);
            if(newRival && oldIndexOfNewRival !== -1){
                //swap
                vm.playerSeedingObject[group][oldIndexOfNewRival] = oldRival;
            }
            
        };
        
        vm.onTeamSelect = function (oldSeeding, newRival, i, group) {
            
            var oldRival = oldSeeding[i];
            //if set to an already chosen value, switch it for its old
            var oldIndexOfNewRival = vm.getIndexById(oldSeeding, newRival);
            if(newRival && oldIndexOfNewRival !== -1){
                //swap
                vm.teamSeedingObject[group][oldIndexOfNewRival] = oldRival;
            }
            
        };
        
        /* *** ELIMINATION seeding section *** */
        /* *** ELIMINATION seeding section *** */
        vm.coordinates = {};
        
        function nodeHTML(i,A,B, group) {
            var id = i+"_"+group;
            return  '<div id="'+id+'" class="tree-node-table not_a_first_round">'+
                        '<div name="A" class="tree-node-table-A">'+vm.getName(A)+'</div>'+
                        '<div name="B" class="tree-node-table-B">'+vm.getName(B)+'</div>'+
                     '</div>';
        }
        function under_dragNodeHTML(i, N, seeding, group){
            var aName = vm.getName(seeding[i]);
            var bName = vm.getName(seeding[N - 1 - i]);
            var id = i+"_"+group;
            return  '<div id="'+id+'" class="tree-node-table .this-is-under">'+
                        '<div name="A" class="tree-node-table-A">'+aName+'</div>'+
                        '<div name="B" class="tree-node-table-B">'+bName+'</div>'+
                     '</div>';
        }
        function parentIndex(i, N) {
            if(i===N-2) return -1;
            return Math.floor((i + N - 2)/2)+1;
        }
        function player_tree(group) {
            console.log(" redrawing tree...");
            
            var containerId = '#player_tree_'+group;
            console.log("containerId: ", containerId);
//            var N = vm.playerSeedingObject[group].length;
            var N = My.getN(vm.groupPlayerObject[group].length);
            
            var config = {container: containerId, rootOrientation: "EAST", nodeAlign: "BOTTOM",
                connectors: {type: 'step'}};
            if(N<=2){
                var only = {innerHTML: under_dragNodeHTML(0, N, vm.playerSeedingObject[group])};
                var nodes = [config, only];
                return nodes;
            }
            
            var numberOfNodes = (N-1);
            var numberOfSelectableNodes = N/2;
            
            var rootID = numberOfNodes-1;
            var root = {innerHTML: nodeHTML(rootID, '', '', group)};
            var nodes = [];
            
            nodes[rootID]=root;
            for (var i = rootID-1 ; i >= 0; i--) {
                var node;
                if(i>=numberOfSelectableNodes){//parents
                    node = {parent: nodes[parentIndex(i, N)], innerHTML: nodeHTML(i, null, null, group)};
                }else{//selectable
                    node = {parent: nodes[parentIndex(i, N)], innerHTML: under_dragNodeHTML(i, N, vm.playerSeedingObject[group], group)};
                }
                nodes[i]=node;
            }
            nodes.push(config);
            nodes.reverse();
            return nodes;
        }
        
        function team_tree(group) {
            console.log(" redrawing tree...");
            
            var containerId = '#team_tree_'+group;
            console.log("containerId: ", containerId);
//            var N = vm.teamSeedingObject[group].length;
            var N = My.getN(vm.groupTeamObject[group].length);
            
            var config = {container: containerId, rootOrientation: "EAST", nodeAlign: "BOTTOM",
                connectors: {type: 'step'}};
            if(N<=2){
                var only = {innerHTML: under_dragNodeHTML(0, N, vm.teamSeedingObject[group])};
                var nodes = [config, only];
                return nodes;
            }
            
            var numberOfNodes = (N-1);
            var numberOfSelectableNodes = N/2;
            
            var rootID = numberOfNodes-1;
            var root = {innerHTML: nodeHTML(rootID, '', '', group)};
            var nodes = [];
            
            nodes[rootID]=root;
            for (var i = rootID-1 ; i >= 0; i--) {
                var node;
                if(i>=numberOfSelectableNodes){//parents
                    node = {parent: nodes[parentIndex(i, N)], innerHTML: nodeHTML(i, null, null, group)};
                }else{//selectable
                    node = {parent: nodes[parentIndex(i, N)], innerHTML: under_dragNodeHTML(i, N, vm.teamSeedingObject[group], group)};
                }
                nodes[i]=node;
            }
            nodes.push(config);
            nodes.reverse();
            return nodes;
        }
        
        vm.initPlayerTree = function (group) {
            angular.element(document).ready(function () {
                if (vm.playerSeedingObject.hasOwnProperty(group)) {
                    new Treant(player_tree(group));
                    getPlayerCoordinatesAndSize(group);
                }
            });
        };
        
        vm.initTeamTree = function (group) {
            angular.element(document).ready(function () {
                if (vm.teamSeedingObject.hasOwnProperty(group)) {
                    new Treant(team_tree(group));
                    getTeamCoordinatesAndSize(group);
                }
            });
        };
        
        function getElementCoordinates(id, group){
            var el = document.getElementById(id+"_"+group);
            var parent = angular.element(el).parent();
            var node = {top: parent.css("top"), left: parent.css("left")};
            vm.coordinates[group][id]=node;
            console.log("    vm.coordinates[",group,"][",id,"]: ", vm.coordinates[group][id]);
        }
        
        function getPlayerCoordinatesAndSize(group) {
            console.log("getPlayerCoordinatesAndSize()");
            var N = My.getN(vm.groupPlayerObject[group].length);
            vm.coordinates[group] = [];
            vm.coordinates[group] = Array.apply(null, Array(N/2)).map(function () {});
            console.log("    N: ", N, ", group: ", group);
            for (var i = 0; i < N/2; i++) {
                if(!vm.coordinates[group][i]){
                    getElementCoordinates(i,group);
                }
            }
            
            //is this necessary?
            vm.treeSize = {};
            var t = angular.element(document.getElementById("player_tree_"+group));
            vm.treeSize["player_tree_"+group] = {};
            vm.treeSize["player_tree_"+group]["width"] = t.css("width");
            vm.treeSize["player_tree_"+group]["height"] = t.css("height"); 
        }
        
        function getTeamCoordinatesAndSize(group) {
            var N = vm.teamSeedingObject[group].length;
            vm.coordinates[group] = [];
            vm.coordinates[group] = Array.apply(null, Array(N/2)).map(function () {});
            for (var i = 0; i < N/2; i++) {
                if(!vm.coordinates[group][i]){
                    getElementCoordinates(i,group);
                }
            }
            //is this necessary?
            vm.treeSize = {};
            var t = angular.element(document.getElementById("team_tree_"+group));
            vm.treeSize["team_tree_"+group] = {};
            vm.treeSize["team_tree_"+group].width = t.css("width");
            vm.treeSize["team_tree_"+group].height = t.css("height");
        }
        
        vm.getSelectedParticipants = function (group) {
            if($scope.chosen === 1) return vm.playerSeedingObject[group];
            if($scope.chosen === 2) return vm.teamSeedingObject[group];
        };
        
        
        
        /* *** end - ELIMINATION seeding section *** */
        /* *** end - ELIMINATION seeding section *** */
        
        



        /** *** end - seeding stuff *** **/

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
