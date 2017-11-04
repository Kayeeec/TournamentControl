(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('EvaluationTableController', EvaluationTableController);

    EvaluationTableController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'My'];

    function EvaluationTableController($scope, $rootScope, $stateParams, previousState, entity, My) {
        var vm = this;
        vm.tournament = entity;

        vm.previousState = previousState.name;
        vm.getName = My.getParticipantName;
        
        var pointsUsed = (vm.tournament.pointsForWinning !== null || vm.tournament.pointsForWinning !== 0)
                        || (vm.tournament.pointsForLosing !== null || vm.tournament.pointsForLosing !== 0)
                        || (vm.tournament.pointsForTie !== null || vm.tournament.pointsForTie !== 0);
        
        
        /* ****** evaluation table ****** */
        
        /*
         * Returns all finished matches for given participant. 
         * 
         * @param {long} participantID participants id
         * @param {Tournament} tournament 
         * @returns {Game[]} array of finished matches 
         */
        function participantsFinishedMatches(participantID, tournament) {
            var finishedMatches = [];
            tournament.matches.forEach(function (match)
            {
                if(match.finished){
                  if ((match.rivalA && match.rivalA.id === participantID) 
                          || (match.rivalB && match.rivalB.id === participantID)) {
                    finishedMatches.push(match);
                    }  
                }
                
            });
            return finishedMatches;
        }
        
        /*
         * Sums scores of both participants in all sets of a match.
         * 
         * @param {Game} match 
         * @returns {tuple/object where A = sum of all participantA scores in all sets, B = same but for participantB}
         */
        var getScoreSum = function (match) {
            var result = {A: 0, B: 0};
            match.sets.forEach(function (set){
                result.A += set.scoreA;
                result.B += set.scoreB;
            });
            return result;
        };
        
        /*
         * Gets statistics of all finished matches for given participant.
         *   
         * @param {long} participantID
         * @returns {object::   rival: string, wins: int, loses: int, ties: int, 
         *                      total: double,  score: double, scoreRival: double }
         *                      rival - set to participants name in view
         */
        $scope.pointCount = function pointCount(participant) {
            var tournament = vm.tournament;
            var hisFinishedMatches = participantsFinishedMatches(participant.id, tournament);
            var points = {rival: vm.getName(participant) ,wins: 0, loses: 0, ties: 0, total: 0, score: 0, scoreRival: 0};

            hisFinishedMatches.forEach(function (match) {
                var scoreSum = getScoreSum(match);
//                classic - score bigger or even
                if(match.rivalA.id === participant.id){
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
                if(match.rivalB.id === participant.id){
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
        
        /**
         * 
         * @param {Object} a = {rival: string, wins: int, loses: int, ties: int, 
         *                      total: double,  score: double, scoreRival: double }
         * @param {type} b
         * @returns {Number}    a, b   : 1
         *                      b, a   :-1
         *                      a == b : 0
         */
        function compare(b, a) { 
            if(a.total > b.total) return 1;
            if(a.total < b.total) return -1;
            
            if(a.wins > b.wins) return 1;
            if(a.wins < b.wins) return -1;
            
            if(a.loses > b.loses) return -1;
            if(a.loses < b.loses) return 1;
            
            if(a.score > b.score) return 1;
            if(a.score < b.score) return -1;
            
            return 0;
            
        }
        
        /*
         * Adds 'rank' attribute to each count object in count_array.
         * @param Array {Object} counts_array 
         * 
         */
        function determineRank(counts_array) {
            if(!counts_array || counts_array.length === 0){
                return;
            }
            var rank = 1;
            counts_array[0].rank = 1;
            for (var i = 1; i < counts_array.length; i++) {
                if(pointsUsed){
                    // just points 
                    if(counts_array[i-1].total !== counts_array[i].total){
                        rank +=1;
                    }
                }else{
                    //use compare (totals will be null)
                    if(compare(counts_array[i-1], counts_array[i]) !== 0){
                        rank +=1;
                    }
                }
                counts_array[i].rank = rank;
            } 
        }
        
        function getAllPointCounts() {
            var counts_array = new Array();
            for (var i = 0; i < vm.tournament.participants.length; i++) {
                var participant = vm.tournament.participants[i];
                var count = $scope.pointCount(participant);
                counts_array.push(count);
            }
            counts_array.sort(compare);
            console.log(counts_array);
            
            determineRank(counts_array);
            return counts_array;
        }
        
        vm.counts = getAllPointCounts();
        
/* ****** END evaluation table ****** */
        
        

        var unsubscribe = $rootScope.$on('tournamentControlApp:participantUpdate', function(event, result) {
            vm.participant = result;
        });
        $scope.$on('$destroy', unsubscribe);
        
        
    }
})();


