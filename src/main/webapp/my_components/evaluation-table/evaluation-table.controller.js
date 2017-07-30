(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('EvaluationTableController', EvaluationTableController);

    EvaluationTableController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity'];

    function EvaluationTableController($scope, $rootScope, $stateParams, previousState, entity) {
        var vm = this;
        vm.tournament = entity;

        vm.previousState = previousState.name;
        
        
        /* ****** evaluation table ****** */
        $scope.temp;
        $scope.counts = new Array();
        function participantsFinishedMatches(participantID, tournament) {
            var r = [];
            tournament.matches.forEach(function (match)
            {
                if(match.finished){
                  if (match.rivalA.id === participantID || match.rivalB.id === participantID) {
                    r.push(match);
                    }  
                }
                
            });
            return r;
        }
        
        var getScoreSum = function (match) {
            var result = {A: 0, B: 0};
            match.sets.forEach(function (set){
                result.A += set.scoreA;
                result.B += set.scoreB;
            });
            return result;
        };
        
        $scope.pointCount = function pointCount(participantID) {
            var tournament = vm.tournament;
            var hisFinishedMatches = participantsFinishedMatches(participantID, tournament);
            var points = {rival: "rival",wins: 0, loses: 0, ties: 0, total: 0, score: 0, scoreRival: 0};

            hisFinishedMatches.forEach(function (match) {
                var scoreSum = getScoreSum(match);
//                classic - score bigger or even
                if(match.rivalA.id === participantID){
                    points.score += scoreSum.A;
                    points.scoreRival += scoreSum.B;
                    
                    if (match.winner === null) {
                        points.ties += 1;
                    } else {
                        if (match.winner.id === match.rivalA.id) {
                            points.wins += 1;
                        }
                        if (match.loser.id === match.rivalA.id) {
                            points.loses += 1;
                        }
                    }
                }
                if(match.rivalB.id === participantID){
                    points.score += scoreSum.B;
                    points.scoreRival += scoreSum.A;
                    
                    if (match.winner === null) {
                        points.ties += 1;
                    } else {
                        if (match.winner.id === match.rivalB.id) {
                            points.wins += 1;
                        }
                        if (match.loser.id === match.rivalB.id) {
                            points.loses += 1;
                        }
                    } 
                }
            });
            points.total = (points.wins * tournament.pointsForWinning) + (points.ties * tournament.pointsForTie)
                    - (points.loses * tournament.pointsForLosing);

            return points;
        };
        
        $scope.emptyCounts = function(){
            $scope.counts = [];
        };
/* ****** END evaluation table ****** */
        
        

        var unsubscribe = $rootScope.$on('tournamentControlApp:participantUpdate', function(event, result) {
            vm.participant = result;
        });
        $scope.$on('$destroy', unsubscribe);
        
        
    }
})();


