(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('all-versus-all', {
            parent: 'entity',
            url: '/all-versus-all',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.allVersusAll.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/all-versus-all/all-versus-alls.html',
                    controller: 'AllVersusAllController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('allVersusAll');
                    $translatePartialLoader.addPart('game');
                    $translatePartialLoader.addPart('gameSet');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('all-versus-all-detail', {
            parent: 'tournament',
            url: '/all-versus-all/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.allVersusAll.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/all-versus-all/all-versus-all-detail.html',
                    controller: 'AllVersusAllDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('allVersusAll');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'AllVersusAll', function($stateParams, AllVersusAll) {
                    return AllVersusAll.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'tournament',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('all-versus-all-detail.edit', {
            parent: 'all-versus-all-detail',
            url: '/all-versus-all/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/all-versus-all/all-versus-all-dialog.html',
                    controller: 'AllVersusAllDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['AllVersusAll', function(AllVersusAll) {
                            return AllVersusAll.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('all-versus-all.new', {
            parent: 'tournament',
            url: '/all-versus-all-new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/all-versus-all/all-versus-all-dialog.html',
                    controller: 'AllVersusAllDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                name: null,
                                note: null,
                                pointsForWinning: null,
                                pointsForLosing: null,
                                pointsForTie: null,
                                created: null,
                                numberOfMutualMatches: null,
                                numberOfSets: null,
                                setsToWin: null,
                                tiesAllowed: null,
                                scoreMax: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('tournament', null, { reload: 'tournament' });
                }, function() {
                    $state.go('tournament');
                });
            }]
        })
        .state('all-versus-all.edit', {
            parent: 'tournament',
            url: '/all-versus-all/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/all-versus-all/all-versus-all-dialog.html',
                    controller: 'AllVersusAllDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['AllVersusAll', function(AllVersusAll) {
                            return AllVersusAll.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('tournament', null, { reload: 'tournament' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('all-versus-all.delete', {
            parent: 'tournament',
            url: '/all-versus-all/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/all-versus-all/all-versus-all-delete-dialog.html',
                    controller: 'AllVersusAllDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['AllVersusAll', function(AllVersusAll) {
                            return AllVersusAll.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('tournament', null, { reload: 'tournament' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
