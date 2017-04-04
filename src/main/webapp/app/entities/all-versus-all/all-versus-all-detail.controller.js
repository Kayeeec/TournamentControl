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

        $scope.pointCount = function pointCount(participantID) {
            var hisFinishedMatches = participantsFinishedMatches(participantID);
//            console.log('hisFinishedMatches: ' + hisFinishedMatches);
            var points = {rival: "rival",wins: 0, loses: 0, ties: 0, total: 0, score: 0, scoreRival: 0};

            hisFinishedMatches.forEach(function (match) {
                if(match.rivalA.id === participantID){
                    if(match.scoreA > match.scoreB){
                        points.wins += 1;
                        points.score += match.scoreA;
                        points.scoreRival += match.scoreB;
                    }
                    if(match.scoreA < match.scoreB){
                        points.loses += 1;
                        points.score += match.scoreA;
                        points.scoreRival += match.scoreB;
                    }
                    if (match.scoreA === match.scoreB) {
                        points.ties += 1;
                        points.score += match.scoreA;
                        points.scoreRival += match.scoreA;
                    }
                    
                }
                if(match.rivalB.id === participantID){
                    if(match.scoreB > match.scoreA){
                        points.wins += 1;
                        points.score += match.scoreB;
                        points.scoreRival += match.scoreA;
                    }
                    if(match.scoreB < match.scoreA){
                        points.loses += 1;
                        points.score += match.scoreB;
                        points.scoreRival += match.scoreA;
                    }
                    if (match.scoreB === match.scoreA) {
                        points.ties += 1;
                        points.score += match.scoreB;
                        points.scoreRival += match.scoreA;
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
