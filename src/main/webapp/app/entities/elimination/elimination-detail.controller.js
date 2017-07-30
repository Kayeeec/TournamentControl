(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('EliminationDetailController', EliminationDetailController);

    EliminationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Elimination', 'Game', '$timeout', '$location'];

    function EliminationDetailController($scope, $rootScope, $stateParams, previousState, entity, Elimination, Game, $timeout, $location) {
        var vm = this;
        vm.elimination = entity;
        vm.previousState = previousState.name;
        vm.publicURL = 'http://localhost:8080/#' + $location.url().toString() + '/public';

        vm.getName = function (rival) {
            if(rival !== null){
                if(rival.player !== null){
                    return rival.player.name;
                }
                if(rival.team !== null){
                    return rival.team.name;
                }
                return 'BYE';
            }
            return '-';
        };
       
        vm.N = vm.elimination.n;
        
        function getRootIndex() {
            if(vm.elimination.type === "SINGLE"){
                return (vm.N - 2);
            }
            if(vm.elimination.type === "DOUBLE"){
                return ((2*vm.N) - 3);
            }
        }
        
        function getParentIndex(index, round) {
            if(vm.elimination.type === "SINGLE"){
                return Math.floor((index + getRootIndex())/2)+1;
            }
            if(vm.elimination.type === "DOUBLE"){
                var winnerRoot = (vm.N - 2);
                var loserRoot = ((2*vm.N)-4);
                
                if(index === winnerRoot || index === loserRoot ){
                    return getRootIndex();
                }
                if(index < winnerRoot){
                    return Math.floor((index + winnerRoot)/2)+1;
                }
                if(index < loserRoot){
                    var segmentSize = vm.N/(Math.pow(2, Math.ceil(Math.abs(round)/10)));
                    if(Math.abs(round) % 10 !== 0){ //15, 25, 35...
                        return (index + segmentSize);   
                    }
                    //else 20,30,40...
                    var inSegmentIndex;
                    if(index % 2 === 1){ 
                        inSegmentIndex = ((index + 1) % segmentSize)/2;
                        return index + segmentSize - inSegmentIndex;
                    }else{
                        inSegmentIndex = ((index) % segmentSize)/2;
                        return (index-1) + segmentSize - inSegmentIndex;
                    }
                }
                if(index === getRootIndex()){
                    if(vm.elimination.bronzeMatch){
                        return ((2*vm.N)-1);
                    }
                    return ((2*vm.N)-2);
                }
            }
        }

        function getElementId(match) {
            var result = '#match_'+match.id.toString();
            return result;
        }
        
        vm.generateNodeStructure = function () {
            var config = {container: '#winner-tree', rootOrientation: "EAST", nodeAlign: "BOTTOM",
                connectors: {type: 'step'} };
            var matches = vm.elimination.matches;
            matches.sort(function(a, b){return a.period - b.period;});
                        
            var rootIndex = getRootIndex();
            var root = {innerHTML: getElementId(matches[rootIndex])};
            var result = [config];
            
            //create nodes in the right order and push them into temporary array 
            var tmpArray = [];
            for (var i = 0; i < rootIndex; i++) {
                var match = matches[i];
                var node = {parent: null, innerHTML: getElementId(match)};
                tmpArray.push(node);
            }
            tmpArray.push(root);
            //define parent of each node in a temporary array, except for the root
            for (var i = 0; i < tmpArray.length - 1; i++) {
                var node = tmpArray[i];
                var parentIndex = getParentIndex((i), matches[i].round);
                node.parent = tmpArray[parentIndex];
            }
            //give root a parent if there is one
            if(isNextFinal(matches.length)){
                console.log("next final is there");
                var nextFinalNode = {innerHTML: getElementId(matches[matches.length-1])};
                root.parent = nextFinalNode;
                tmpArray.push(nextFinalNode);
            }
            //push nodes from temporary array to result in reversed order (Treant.js takes them like that)
            while (tmpArray.length > 0) {
                result.push(tmpArray.pop());
            }
            return result;
        };
        
        function isNextFinal(matchesLength) {
            if(vm.elimination.bronzeMatch){
                return (matchesLength === 2 * vm.N);
            }
            return (matchesLength === (2*vm.N - 1));
        }
        
        vm.generateBronzeNodeStructure = function () {
            var matches = vm.elimination.matches;
            matches.sort(function(a, b){return a.period - b.period;});
            
            var config = {container: '#bronze-tree', rootOrientation: "EAST", nodeAlign: "BOTTOM",
                connectors: {type: 'step'} };
            var bronzeNode = {innerHTML: getElementId(matches[getRootIndex() + 1])};
            
            return [config, bronzeNode];
            
        };
        
        function loadTrees() {
            $timeout(function () { //has to be called after the DOM is rendered
                if (vm.elimination.matches.length > 0) {
                    vm.winnerTree = new Treant(vm.generateNodeStructure());
                    if(vm.elimination.bronzeMatch){
                        vm.bronzeTree = new Treant(vm.generateBronzeNodeStructure());
                    }
                }
            });
        }
        
        $scope.$on('$viewContentLoaded', function () {
            loadTrees();
        });
        
        var unsubscribe = $rootScope.$on('tournamentControlApp:eliminationUpdate', function(event, result) {
            vm.elimination = result;
            loadTrees();
        });
        $scope.$on('$destroy', unsubscribe);
        
        vm.disabledIfBYE = function (match) {
            if((match.rivalA !== null && match.rivalA.bye) || (match.rivalB !== null && match.rivalB.bye)){
                return 'mylinkDisabled';
            }
            return '';
        };
        
        vm.greenIfFinished = function (match) {
            if(match.finished){
                return 'bracket-match-finished';
            }
            return '';
            
        };
        
        /* *** sending dto pokus TODO: delete when seeding finished*** */
        vm.sendDTO = function () {
            var seeding = [];
            seeding.push(angular.copy(vm.elimination.participants[0]));
            seeding.push(angular.copy(vm.elimination.participants[1]));
            
            var eliminationDTO = {
                elimination: vm.elimination,
                seeding: seeding
            };
            console.log(eliminationDTO);
            Elimination.sendDTO(eliminationDTO);
        };
        
        
    }
})();
