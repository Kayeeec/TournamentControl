(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('CombinedEvaluationTableController', CombinedEvaluationTableController);

    CombinedEvaluationTableController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'My'];

    function CombinedEvaluationTableController($scope, $rootScope, $stateParams, previousState, entity, My) {
        var vm = this;
        vm.combined = entity;

        vm.previousState = previousState.name;
        
        var unsubscribe = $rootScope.$on('tournamentControlApp:combinedUpdate', function(event, result) {
            vm.combined = result;
        });
        $scope.$on('$destroy', unsubscribe);
        
        /**
         * object 
         * { participant.id : pointCountObject }
         * 
         * pointCountObject {
         *  rank:
         *  name : 
         *  wins:
         *  loses:
         *  ties:
         *  score:
         *  rivalScore:
         *  points:
         * }
         */
        vm.evaluation = init_evaluation();
        
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
            if(!match || !match.finished){
                return;
            }
            //is finished 
            var score = getSumScore(match);
            
            console.log("match: ", match);
            if (!match.rivalA.bye) {
                vm.evaluation[match.rivalA.id].score += score.A;
                vm.evaluation[match.rivalA.id].rivalScore += score.B;
            }
            if (!match.rivalB.bye) {
                vm.evaluation[match.rivalB.id].score += score.B;
                vm.evaluation[match.rivalB.id].rivalScore += score.A;
            }
            //tie - finished and winner and loser are null, only one of them being null should not happen
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
            
            //loser and don't forget bye 
            if(!match.loser.bye){
                vm.evaluation[match.loser.id].loses += 1;
                vm.evaluation[match.loser.id].points -= tournament.pointsForLosing;
            }
            
        }
        
        function getSumScore(match) {
            var result = {A: 0, B: 0};
            for (var s = 0; s < match.sets; s++) {
                var set = match.sets[s];
                result.A += set.scoreA;
                result.B += set.scoreB; 
            }
            return result;
        }
        
        /**
         * 
         * @param {Object} a = {rival: string, wins: int, loses: int, ties: int, 
         *                      total: double,  score: double, scoreRival: double }
         * @param {type} b
         * @returns {Number}    a, b   : 1
         *                      b, a   :-1
         *                      a == b : 0
         */
        function pointCount_compare(b, a) { 
            if(a.points > b.points) return 1;
            if(a.points < b.points) return -1;
            
            if(a.wins > b.wins) return 1;
            if(a.wins < b.wins) return -1;
            
            if(a.loses > b.loses) return -1;
            if(a.loses < b.loses) return 1;
            
            if(a.score/a.rivalScore > b.score/b.rivalScore) return 1;
            if(a.score/a.rivalScore < b.score/b.rivalScore) return -1;
            
            return 0;
            
        }
        
        function notCompletelyEqual(prev, pointCount) {
            return prev.points !== pointCount.points
                        || prev.wins !== pointCount.wins
                        || prev.loses !== pointCount.loses
                        || prev.score !== pointCount.score;
        }
        
        
        function preparePointCountArray() {
            evaluate();
            
            var pointcounts_arr = [];
            
            for (var pointcount in vm.evaluation) {
                pointcounts_arr.push(vm.evaluation[pointcount]);
            }
            
            pointcounts_arr.sort(pointCount_compare);
            console.log("pointcounts_arr: ", pointcounts_arr);
            
            //compute ranks
            for (var i = 1; i < pointcounts_arr.length; i++) {
                var prev = pointcounts_arr[i-1];
                var pointCount = pointcounts_arr[i];
                console.log("pointCount: ", pointCount);;
                
                pointCount["rank"] = prev.rank;
                
                if(notCompletelyEqual(prev, pointCount)){
                    pointCount["rank"]  += 1;
                }
            }
            
            return pointcounts_arr;
        }
        
        
    }
})();


