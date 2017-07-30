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
        
        vm.participants = Participant.query();
        
    /* *** initiating default values *** */
        vm.elimination.bronzeMatch = vm.elimination.bronzeMatch || false;
        vm.elimination.type = vm.elimination.type || "SINGLE";
        vm.elimination.tiesAllowed = false;
    /* *** END: initiating default values *** */
        
    /* participants stuff */
        vm.elimination.participants = vm.elimination.participants || [];
        vm.selectedPlayers = filterFilter(vm.elimination.participants, {team : null});
        vm.selectedTeams = filterFilter(vm.elimination.participants, {player : null});
        $scope.chosen = 1;
        
        function selectParticipants() {
            if($scope.chosen === 1){
                angular.copy(vm.selectedPlayers, vm.elimination.participants);
            }
            if($scope.chosen === 2){
                angular.copy(vm.selectedTeams, vm.elimination.participants);
            }
        }
        
        function selectSelectedParticipants() {
            if($scope.chosen === 1){
                angular.copy(vm.selectedPlayers, vm.selectedParticipants);
            }
            if($scope.chosen === 2){
                angular.copy(vm.selectedTeams, vm.selectedParticipants);
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
        
    /* *** participant seeding stuff *** */
        function init_seeding() {
            if(vm.elimination.id){
                return Elimination.getSeeding({id: vm.elimination.id});
            }
            return [];
        }
        
        function init_seedRandomly() {
            if(vm.elimination.id === null){ //new tournament
                return true;
            }
            return false; //editing old tournament
        }
        
        vm.seeding = init_seeding();
        vm.selectedParticipants = angular.copy(vm.elimination.participants);
        $scope.seedRandomly = init_seedRandomly();
        vm.coordinates = [];
        vm.changed = false;
        
        vm.setParticipantsChanged = function () {
            if(!vm.changed){
                vm.changed = true;
            }
            console.log("vm.setParticipantsChanged called, vm.changed = ", vm.changed);
        };
        
        function nodeHTML(id,A,B) {
            return  `<div id="${id}" class="tree-node-table">
                        <div name="A" class="tree-node-table-A"> ${vm.getName(A)} </div>
                        <div name="B" class="tree-node-table-A"> ${vm.getName(B)} </div>
                     </div>`;
        }
        
        function under_dragNodeHTML(i, N){
            var aName = vm.getName(vm.seeding[i]);
            var bName = vm.getName(vm.seeding[N - 1 - i]);
            return  `<div id="${i}" class="tree-node-table">
                        <div name="A" class="tree-node-table-A"> ${aName} </div>
                        <div name="B" class="tree-node-table-A"> ${bName} </div>
                     </div>`;
        }
        
        function parentIndex(i, N) {
            if(i===N-2) return -1;
            return Math.floor((i + N - 2)/2)+1;
        }
        
        function tree(N) {
            var config = {container: '#tree', rootOrientation: "EAST", nodeAlign: "BOTTOM",
                connectors: {type: 'step'} };
            if(N<=2){
                var only = {innerHTML: under_dragNodeHTML(0, N)};
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
                    node = {parent: nodes[parentIndex(i, N)], innerHTML: under_dragNodeHTML(i, N)};
                }
                nodes[i]=node;
            }
            nodes.push(config);
            nodes.reverse();
            return nodes;
        }
        
        vm.prepareSeeding = function () {
            if($scope.seedRandomly){
                return;
            }
            console.log("vm.prepareSeeding proceeds");
            selectSelectedParticipants();
            if(vm.selectedParticipants && vm.selectedParticipants.length < 3){
                $scope.seedRandomly = true;
                return;
            }
            var n = vm.selectedParticipants.length;
            
            $scope.N = My.getN(n);
            $scope.halfN = $scope.N/2;
            var byesNum = $scope.N - n;
            if(vm.changed){
                vm.seeding.length = $scope.N;
                vm.seeding.fill(null,0,$scope.N);
                console.log("changed is true, seeding = ", vm.seeding);
            }
            if(byesNum > 0){
                var BYE = Participant.getBye();
                for (var i = 0; i < byesNum; i++) {
                    vm.seeding[$scope.N-1-i] = BYE;
                }
            }
            
            new Treant(tree($scope.N));
            
            getCoordinatesAndSize($scope.N);
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
        
        function getCoordinatesAndSize(N) {
            vm.coordinates = Array.apply(null, Array(N/2)).map(function () {});
            for (var i = 0; i < N/2; i++) {
                if(!vm.coordinates[i]){
                    getElementCoordinates(i);
                }
            }
            
            vm.treeSize = {};
            var t = angular.element(document.getElementById("tree"));
            vm.treeSize.width = t.css("width");
            vm.treeSize.height = t.css("height"); 
        }
        
        vm.contains = function (array, elem) {
            for (var i = 0; i < array.length; i++) {
                if(!elem){ // elem === null
                    if(!array[i]){
                        return true;
                    }
                }
                else if (array[i] && array[i].id === elem.id){
                    return true;
                }
            }
            return false;
        };
        
        vm.onParticipantSelect = function (oldSeeding, newRival, i) {
            var oldRival = oldSeeding[i];
            //if set to an already chosen value, switch it for its old
            if(newRival && vm.contains(oldSeeding, newRival)){
                var oldIndexOfNewRival = oldSeeding.findIndex(item => item && item.id === newRival.id);
                //swap
                vm.seeding[oldIndexOfNewRival] = oldRival;
            }
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
        
        $('#myTab a[href="#players"]').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });
        $('#myTab a[href="#teams"]').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });
        
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
            if($scope.seedRandomly){
                vm.eliminationDTO.seeding = null;
            }else {
                vm.eliminationDTO.seeding = vm.seeding;
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
        
        vm.invalidSeeding = function () {
            if(!$scope.seedRandomly){
                if(vm.seeding){
                    console.log(vm.contains(vm.seeding, null));
                    return vm.contains(vm.seeding, null);
                }
                //seeding is null > invalid
                return true;
            }
            //seeding is random > by computer so dont worry about it 
            return false; 
        };

    }
})();
