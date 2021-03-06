/* 
 * author: Karolina Bozkova
 */
(function () {
    'use strict';
    angular
            .module('tournamentControlApp')
            .factory('Evaluation', Evaluation);

    Evaluation.$inject = [];
    
    /**
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
   
    function getScoreComparison(prev, pointcount){
        var prevRatio;
        if(prev.rivalScore === 0){
            prevRatio = prev.score;
        }else{
            prevRatio = prev.score/prev.rivalScore;
        }
        var pointCountRatio;
        if(pointcount.rivalScore === 0){
            pointCountRatio = pointcount.score;
        }else{
            pointCountRatio = pointcount.score/pointcount.rivalScore;
        }
        return prevRatio !== pointCountRatio;
    }

    function Evaluation() {
        return {
            'getSumScore': function getSumScore(match) {
                var result = {A: 0, B: 0};
                for (var s = 0; s < match.sets.length; s++) {
                    var set = match.sets[s];
                    result.A += set.scoreA;
                    result.B += set.scoreB; 
                }
                return result;
            },
            'pointCount_compare': function (b,a) {
                return pointCount_compare(b, a);
            },
            'notCompletelyEqual': function notCompletelyEqual(prev, pointCount) {
                
                var scoreComparison = getScoreComparison(prev, pointCount);
                                
                var result = prev.points !== pointCount.points
                            || prev.wins !== pointCount.wins
                            || prev.loses !== pointCount.loses
                            || scoreComparison;
                    
                console.log('notCompletelyEqual = ', result);
                console.log("prev: ", prev);
                console.log("pointCount: ", pointCount);
                
                return result;
            }
        
        
        };
    }
})();


