(function () {
    'use strict';
    angular
            .module('tournamentControlApp')
            .factory('My', My);

    My.$inject = ['$window'];
    
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
    
    function contains(array, elem) {
        for (var i = 0; i < array.length; i++) {
            if(!elem){ // elem === null
                if(!array[i]){
                    return true;
                }
            }
            else if (array[i] && array[i].id === elem.id){
                return true;
            }
        }
        return false;
    }
    
    function getIndexById(array,elem){
        for (var i = 0; i < array.length; i++) {
            if(elem && array[i] && elem.id===array[i].id){
                return i;
            }
        }
        return -1;
    }

    function My($window) {
        return {
            'getParticipantName': function (participant) {
                return getParticipantName(participant);
            },
            'containsElemWithId': function (array, elem) {
                return contains(array,elem);
            },
            'getIndexById': function(array,elem) {
                return getIndexById(array,elem);
            },
            'getSeedingOptionName': function getSeedingOptionName(seeding, participant) {
                if(participant){
                        if(contains(seeding, participant)){
                        return participant.name;
                    }
                    return participant.name +" (not seeded)";
                }
                return "";
            },
            'getN': function getN(n) {
                if ((n & (n - 1)) === 0) {
                return n;
                }
                while ((n & (n - 1)) !== 0) {
                    n = n & (n - 1);
                }
                n = n << 1;
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
            },
            'getTournamentLink': function getTournamentLink(tournament) {
                switch (tournament.tournamentType) {
                    case 'ALL_VERSUS_ALL':
                        return "all-versus-all-detail({id:"+tournament.id+"})";
                    case 'SWISS':
                        return "swiss-detail({id: "+tournament.id+"})";
                        
                    default: //ELIMINATION
                        return "elimination-detail({id: "+tournament.id+"})";
                }
            },
            'getCombinedTournamentLink': function getCombinedTournamentLink(tournament) {
                return "combined-detail({id:"+tournament.id+"})";
            },
            'backLink': function back(previousState) {
                if(previousState.params.hasOwnProperty("id")){
                    return previousState.name+"({id:"+previousState.params.id+"})";
                }
                return previousState.name;
            },
            'savePreviousUrl': function savePreviousUrl(previousState) {
                if(previousState.url){
                    window.localStorage.setItem("url",previousState.url);
                }
            }
            
        };
    }
})();
