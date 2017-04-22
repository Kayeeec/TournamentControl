(function () {
    'use strict';

    angular
            .module('tournamentControlApp')
            .controller('AllVersusAllDetailController', AllVersusAllDetailController);

    AllVersusAllDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'AllVersusAll', 'Game', 'User', 'Participant', '$resource'];

    function AllVersusAllDetailController($scope, $rootScope, $stateParams, previousState, entity, AllVersusAll, Game, User, Participant, $resource) {
        var vm = this;

        vm.allVersusAll = entity;
        vm.previousState = previousState.name;
        
        $scope.temp;
        $scope.counts = new Array();
        

        function participantsFinishedMatches(participantID) {
            var r = [];
            vm.allVersusAll.matches.forEach(function (match)
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
            for (var set in match.sets) {
                result.A += set.scoreA;
                result.B += set.scoreB;
            }
            return result;
        };
        
        

        $scope.pointCount = function pointCount(participantID) {
            var hisFinishedMatches = participantsFinishedMatches(participantID);
//            console.log('hisFinishedMatches: ' + hisFinishedMatches);
            var points = {rival: "rival",wins: 0, loses: 0, ties: 0, total: 0, score: 0, scoreRival: 0};

            hisFinishedMatches.forEach(function (match) {
                var scoreSum = getScoreSum(match);
//                classic - score bigger or even
                if(match.rivalA.id === participantID){
                    points.score += scoreSum.A;
                    points.scoreRival += scoreSum.B;
                    if(scoreSum.A > scoreSum.B){
                        points.wins += 1;
                    }
                    if(scoreSum.A < scoreSum.B){
                        points.loses += 1;
                    }
                    if (scoreSum.A === scoreSum.B) {
                        points.ties += 1;
                    }   
                }
                if(match.rivalB.id === participantID){
                    points.score += scoreSum.B;
                    points.scoreRival += scoreSum.A;
                    if(scoreSum.B > scoreSum.A){
                        points.wins += 1;
                    }
                    if(scoreSum.B < scoreSum.A){
                        points.loses += 1;
                    }
                    if (scoreSum.B === scoreSum.A) {
                        points.ties += 1;
                    }
                }
            });
            points.total = (points.wins * vm.allVersusAll.pointsForWinning) + (points.ties * vm.allVersusAll.pointsForTie)
                    - (points.loses * vm.allVersusAll.pointsForLosing);

            return points;
        };
        
        $scope.emptyCounts = function(){
            $scope.counts = [];
        };
        

        var unsubscribe = $rootScope.$on('tournamentControlApp:allVersusAllUpdate', function (event, result) {
            vm.allVersusAll = result;
            $scope.counts = [];
            console.log('on update counts: ' + $scope.counts);
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
