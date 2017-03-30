(function () {
    'use strict';

    angular
            .module('tournamentControlApp')
            .controller('AllVersusAllDetailController', AllVersusAllDetailController);

    AllVersusAllDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'AllVersusAll', 'Game', 'User', 'Participant', 'filterFilter'];

    function AllVersusAllDetailController($scope, $rootScope, $stateParams, previousState, entity, AllVersusAll, Game, User, Participant, filterFilter) {
        var vm = this;

        vm.allVersusAll = entity;
        vm.previousState = previousState.name;
        $scope.matches = vm.allVersusAll.matches;

        $scope.finishedMatchesArr = filterFilter($scope.matches, {finished: true});

        function participantsFinishedMatches(participantID) {
            var r = [];
            $scope.finishedMatchesArr.forEach(function (match)
            {
                if (match.rivalA.id === participantID || match.rivalB.id === participantID) {
                    r.push(match);
                }
            });
            return r;
        }

        $scope.pointCount = function pointCount(participantID) {
            var hisFinishedMatches = participantsFinishedMatches(participantID);
            var points = {wins: 0, loses: 0, ties: 0, total: 0};

            hisFinishedMatches.forEach(function (match) {
                if (match.rivalA.id === participantID && match.scoreA > match.scoreB) {
                    points.wins += 1;
                }
                if (match.rivalB.id === participantID && match.scoreB > match.scoreA) {
                    points.wins += 1;
                }
                if (match.rivalA.id === participantID && match.scoreA < match.scoreB) {
                    points.loses += 1;
                }
                if (match.rivalB.id === participantID && match.scoreB < match.scoreA) {
                    points.loses += 1;
                }
                if (match.scoreA === match.scoreB) {
                    points.ties += 1;
                }
            });
            points.total = (points.wins * vm.allVersusAll.pointsForWinning) + (points.ties * vm.allVersusAll.pointsForTie)
                    - (points.loses * vm.allVersusAll.pointsForLosing);

            return points;
        };

        $scope.winCount = function winCount(participantID) {
            var hisFinishedMatches = participantsFinishedMatches(participantID);
            var wins = 0;

            hisFinishedMatches.forEach(function (match) {
                if (match.rivalA.id === participantID && match.scoreA > match.scoreB) {
                    wins += 1;
                }
                if (match.rivalB.id === participantID && match.scoreB > match.scoreA) {
                    wins += 1;
                }

            });

            return wins;
        };
        $scope.lossCount = function lossCount(participantID) {
            var hisFinishedMatches = participantsFinishedMatches(participantID);
            var loses = 0;

            hisFinishedMatches.forEach(function (match) {

                if (match.rivalA.id === participantID && match.scoreA < match.scoreB) {
                    loses += 1;
                }
                if (match.rivalB.id === participantID && match.scoreB < match.scoreA) {
                    loses += 1;
                }
            });

            return loses;
        };

        $scope.tieCount = function tieCount(participantID) {
            var hisFinishedMatches = participantsFinishedMatches(participantID);
            var ties = 0;

            hisFinishedMatches.forEach(function (match) {
                if (match.scoreA === match.scoreB) {
                    ties += 1;
                }
            });
            return ties;
        };


//        function pointCountB(participant) {
//            var hisFinishedMatches = participantsFinishedMatches(participant);
//            var points = {rival: participant, wins: 0, loses: 0, ties: 0, total: 0};
//
//            hisFinishedMatches.forEach(function (match) {
//                if (match.rivalA.id === participant.id && match.scoreA > match.scoreB) {
//                    points.wins += 1;
//                }
//                if (match.rivalB.id === participant.id && match.scoreB > match.scoreA) {
//                    points.wins += 1;
//                }
//                if (match.rivalA.id === participant.id && match.scoreA < match.scoreB) {
//                    points.loses += 1;
//                }
//                if (match.rivalB.id === participant.id && match.scoreB < match.scoreA) {
//                    points.loses += 1;
//                }
//                if (match.scoreA === match.scoreB) {
//                    points.ties += 1;
//                }
//            });
//            points.total = (points.wins * vm.allVersusAll.pointsForWinning) + (points.ties * vm.allVersusAll.pointsForTie)
//                    - (points.loses * vm.allVersusAll.pointsForLosing);
//
//            return points;
//        };
//
//
//        function evaluateParticipants() {
//            var p = [];
//            for (var participant in vm.allVersusAll.participants) {
//                p.push(pointCountB(participant));
//            }
//            return p;
//        }
//        ;



        var unsubscribe = $rootScope.$on('tournamentControlApp:allVersusAllUpdate', function (event, result) {
            vm.allVersusAll = result;
        });
        $scope.$on('$destroy', unsubscribe);
        
//        $scope.evaluated;
//        function init() {
//            $scope.evaluated = evaluateParticipants();
//        }
//
//        window.onload = init;
    }
})();
