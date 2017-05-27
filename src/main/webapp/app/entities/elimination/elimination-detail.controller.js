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
            if(vm.elimination.bronzeMatch){
                return vm.elimination.matches.length - 2;
            }else {
                return vm.elimination.matches.length - 1;  
            }
        }

        function getElementId(match) {
            var result = '#match_'+match.id.toString();
            console.log(result);
            return result;
        }
        
        vm.generateNodeStructure = function () {
            var config = {container: '#winner-tree', rootOrientation: "EAST", 
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
            //define parent of each node in a temporary array, exept for the root
            for (var i = 0; i < tmpArray.length - 1; i++) {
                var node = tmpArray[i];
                var parentIndex = Math.floor((i + rootIndex)/2)+1;
                node.parent = tmpArray[parentIndex];
            }
            //push nodes from temporary array to result in reversed order
            while (tmpArray.length > 0) {
                result.push(tmpArray.pop());
            }
            return result;
        };
        
        $scope.$on('$viewContentLoaded', function (event)
        {
            $timeout(function () { //has to be called after the DOM is rendered
                if (vm.elimination.matches.length > 0) {
                    vm.winnerTree = new Treant(vm.generateNodeStructure());
                }
            });

        });
        
        var unsubscribe = $rootScope.$on('tournamentControlApp:eliminationUpdate', function(event, result) {
            vm.elimination = result;
        });
        $scope.$on('$destroy', unsubscribe);
        
        
    }
})();
