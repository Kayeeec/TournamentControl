(function () {
    'use strict';
    angular
            .module('tournamentControlApp')
            .factory('My', My);

    My.$inject = [];
    
    function getParticipantName(participant) {
        if(participant){
            if(participant.player){
                return participant.player.name;
            }
            if(participant.team){
                return participant.team.name;
            }
            return 'BYE';
        }
        return '-';
    }

    function My() {
        return {
            'getParticipantName': function getParticipantName(participant) {
                if(participant){
                    if(participant.player){
                        return participant.player.name;
                    }
                    if(participant.team){
                        return participant.team.name;
                    }
                    return 'BYE';
                }
                return '-';
            },
            'getN': function getN(n) {
                if ((n & (n - 1)) === 0) {
                return n;
                }
                while ((n & (n - 1)) !== 0) {
                    n = n & (n - 1);
                }
                n = n << 1;
                console.log("N = " + n);
                return n;
            },
            'printRivals': function printRivals(list) {
                if(!list) return '';
                var string = "[";
                for (var i = 0; i < list.length; i++) {
                    string = string.concat(getParticipantName(list[i]));
                    if(i !== list.length - 1){
                        string = string.concat(", ");
                    }
                }
                string = string.concat("]");
                return string;
            },
            'isBye': function isBye(rival){
                if(rival){
                    return rival.bye;
                }
                return false;
            },
            'iterator': function iterator(from, to, step){
                var array = [];
                for (var i = from; i < to; i+=step) {
                    array[i]=i;
                }
                return array;
            }
        };
    }
})();
