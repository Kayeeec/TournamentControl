(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('EliminationDetailController', EliminationDetailController);

    EliminationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Elimination'];

    function EliminationDetailController($scope, $rootScope, $stateParams, previousState, entity, Elimination) {
        var vm = this;

        vm.elimination = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('tournamentControlApp:eliminationUpdate', function(event, result) {
            vm.elimination = result;
        });
        $scope.$on('$destroy', unsubscribe);
        
        /* brackets ================================================================== */
        $scope.chooseElimination = function () {
            var single = [];
            var double = [[[[]]], [], []];
            console.log(vm.elimination.eliminationType);
             if(vm.elimination.eliminationType === "SINGLE"){
                 return single;
             }else {
                return double;
            }

        };
        
        function getName(p) {
            if(p.player !== null){
                return p.player.name;
            }else {
                return p.team.name;
            }

        }
 
        $scope.randomAssign = function () {
            var teams = [];
            var n;
            if(vm.elimination.participants.length % 2 === 1){
                n = (vm.elimination.participants.length + 1)/2;
            }else {
                 n = (vm.elimination.participants.length)/2;
            }
//            init
            for (var i = 0, max = n; i < n; i++) {
                teams.push([null,null]);
            }
            
            var participantStack = [];
            angular.copy(vm.elimination.participants, participantStack);
            
//            eval firsts
            teams.forEach(function(pair) {
                if(participantStack.length !== 0) {
                    var p = participantStack.pop();
                    pair[0] = getName(p);
                }
                
            });
//            eval seconds
            teams.forEach(function(pair) {
                if(participantStack.length !== 0) {
                    var p = participantStack.pop();
                    pair[1] = getName(p);
                }
                
            });
            return teams;
        };
 
        var saveData = {
            teams: $scope.randomAssign(),
            results: []
        };

        /* Called whenever bracket is modified
         *
         * data:     changed bracket object in format given to init
         * userData: optional data given when bracket is created.
         */
        function saveFn(data, userData) {
            var json = JSON.stringify(data);
            $('#saveOutput').text('POST ' + userData + ' ' + json);
            /* You probably want to do something like this
             jQuery.ajax("rest/"+userData, {contentType: 'application/json',
             dataType: 'json',
             type: 'post',
             data: json})
             */
            console.log("usrData = "+userData);
            console.log("data = "+JSON.stringify(data));
        }

        $(function () {
            var container = $('div#save');
            container.bracket({
                centerConnectors: true,
                init: saveData,
                save: saveFn,
                userData: "http://myapi"});

            /* You can also inquiry the current data */
            var data = container.bracket('data');
            $('#dataOutput').text(JSON.stringify(data));
        });
 /* brackets ================================================================== */
    }
})();
