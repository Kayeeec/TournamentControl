(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TeamDialogController', TeamDialogController);

    TeamDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'Team', 'User', 'Player'];

    function TeamDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, Team, User, Player) {
        var vm = this;

        vm.team = entity;
        vm.clear = clear;
        vm.save = save;
        vm.players = Player.query();
        vm.originalMembers = angular.copy(vm.team.members);
        
        console.log(vm.team.members);

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.team.id !== null) {
                Team.update(vm.team, onSaveSuccess, onSaveError);
            } else {
                Team.save(vm.team, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('tournamentControlApp:teamUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }
        
        /* *** member selecting methods *** */
        vm.clearMembers = function () {
            vm.team.members.length = 0;
        };
        
        vm.resetMembers = function () {
            vm.team.members = angular.copy(vm.originalMembers);
        };
        
        vm.focusMembers = function () {
            $scope.$broadcast('myMembersPlaceholderClicked');
        };
        vm.isOpen = false;
        vm.onOpenClose = function (isOpen) {
            vm.isOpen = isOpen;
        };


    }
})();
