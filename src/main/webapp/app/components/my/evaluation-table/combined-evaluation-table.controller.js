(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('CombinedEvaluationTableController', CombinedEvaluationTableController);

    CombinedEvaluationTableController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Evaluation'];

    function CombinedEvaluationTableController($scope, $rootScope, $stateParams, previousState, entity, Evaluation) {
        var vm = this;
        vm.combined = entity;

        vm.previousState = previousState.name;
        
        var unsubscribe = $rootScope.$on('tournamentControlApp:combinedUpdate', function(event, result) {
            vm.combined = result;
        });
        $scope.$on('$destroy', unsubscribe);
        
        /**
         * contains computed evaluation data 
         * 
         * object 
         * { participant.id : pointCountObject }
         * 
         * pointCountObject {
         *       rank:
         *       name : 
         *       wins:
         *       loses:
         *       ties:
         *       score:
         *       rivalScore:
         *       points:
         * }
         */
        vm.evaluation = init_evaluation();
        
        /**
         * for storing sorted evaluation data and computing rank 
         */
        vm.pointCountArray = preparePointCountArray();
        
        function init_evaluation() {
            console.log("init_evaluation()");
            var result = {};
            
            for (var i = 0; i < vm.combined.allParticipants.length; i++) {
                var participant = vm.combined.allParticipants[i];
                var pointCount = {
                    rank: 1,
                    name: participant.name,
                    wins: 0,
                    loses: 0,
                    ties: 0,
                    score: 0,
                    rivalScore: 0,
                    points: 0
                };
                
                result[participant.id] = pointCount;
            }
            console.log(result);
            return result;
        }
        
        function evaluate(){
            console.log("vm.evaluate()");
            //groups
            if (vm.combined.groups) {
                for (var g = 0; g < vm.combined.groups.length; g++) {
                    var group = vm.combined.groups[g];
                    if (group.matches) {
                        for (var m = 0; m < group.matches.length; m++) {
                            var match = group.matches[m];
                            evaluateMatch(group, match);
                        }
                    } 
                }
            }
            
            //playoff
            if (vm.combined.playoff && vm.combined.playoff.matches) {
                for (var pm = 0; pm < vm.combined.playoff.matches.length; pm++) {
                    var playoff_match = vm.combined.playoff.matches[pm];
                    evaluateMatch(vm.combined.playoff, playoff_match);
                }
            }
        };
        
        function evaluateMatch(tournament, match) {
            if(!match || !match.finished 
                    || (match.rivalA && match.rivalA.bye)
                    || (match.rivalB && match.rivalB.bye) ){
                return; //ignores bye matches
            }
            //is finished 
            var score = Evaluation.getSumScore(match);
            
            vm.evaluation[match.rivalA.id].score += score.A;
            vm.evaluation[match.rivalA.id].rivalScore += score.B;
            vm.evaluation[match.rivalB.id].score += score.B;
            vm.evaluation[match.rivalB.id].rivalScore += score.A;
            //tie - finished && winner and loser are null, only one of them being null should not happen
            if(match.winner === null && match.loser === null){
                vm.evaluation[match.rivalA.id].ties += 1;
                vm.evaluation[match.rivalA.id].points += tournament.pointsForTie;
                
                vm.evaluation[match.rivalB.id].ties += 1;
                vm.evaluation[match.rivalB.id].points += tournament.pointsForTie;
                return;
            }
            //winner
            vm.evaluation[match.winner.id].wins += 1;
            vm.evaluation[match.winner.id].points += tournament.pointsForWinning;
            
            //loser
            vm.evaluation[match.loser.id].loses += 1;
            vm.evaluation[match.loser.id].points -= tournament.pointsForLosing;
            
        }
        
        function preparePointCountArray() {
            evaluate();
            
            var pointcounts_arr = [];
            
            for (var pointcount in vm.evaluation) {
                pointcounts_arr.push(vm.evaluation[pointcount]);
            }
            
            pointcounts_arr.sort(Evaluation.pointCount_compare);
            console.log("pointcounts_arr: ", pointcounts_arr);
            
            //compute ranks
            for (var i = 1; i < pointcounts_arr.length; i++) {
                var prev = pointcounts_arr[i-1];
                var pointCount = pointcounts_arr[i];
                console.log("pointCount: ", pointCount);;
                
                pointCount["rank"] = prev.rank;
                
                if(Evaluation.notCompletelyEqual(prev, pointCount)){
                    pointCount["rank"]  += 1;
                }
            }
            
            return pointcounts_arr;
        }
        
        
    }
})();


