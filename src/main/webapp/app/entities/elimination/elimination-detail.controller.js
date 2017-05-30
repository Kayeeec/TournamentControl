(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('EliminationDetailController', EliminationDetailController);

    EliminationDetailController.$inject = ['$timeout','$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Elimination'];

    function EliminationDetailController($timeout, $scope, $rootScope, $stateParams, previousState, entity, Elimination) {
        var vm = this;

        vm.elimination = entity;
        vm.previousState = previousState.name;
        
        
        function getNextPowerOfTwo(n) {
            if ((n & (n - 1)) === 0) {
                return n;
            }
            while ((n & (n - 1)) !== 0) {
                n = n & (n - 1);
            }
            n = n << 1;
            console.log("N = " + n);
            return n;
        }
        
        vm.N = getNextPowerOfTwo(vm.elimination.participants.length);
        console.log("N: "+vm.N);
                
        vm.getName = function (rival, round) {
            if(rival !== null){
                if(rival.player !== null){
                    return rival.player.name;
                }
                return rival.team.name;
            }
            if(round === 1){
                return 'BYE';
            }
            return '-';
        };
        
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
                    var segmentSize = vm.N/(Math.pow(2, Math.ceil(round/10)));
                    if(round % 10 !== 0){ //15, 25, 35...
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
            //push nodes from temporary array to result in reversed order (Treant.js takes them like that)
            while (tmpArray.length > 0) {
                result.push(tmpArray.pop());
            }
            return result;
        };
        
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
            if(match.round === 1){
                if(match.rivalA === null || match.rivalB === null){
                    return 'mylinkDisabled';
                }
            }
            return '';
        };
        
        
    }
})();
