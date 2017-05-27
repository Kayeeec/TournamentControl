(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('elimination', {
            parent: 'entity',
            url: '/elimination',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.elimination.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/elimination/eliminations.html',
                    controller: 'EliminationController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('elimination');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('elimination-detail', {
            parent: 'tournament',
            url: '/elimination/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.elimination.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/elimination/elimination-detail.html',
                    controller: 'EliminationDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('elimination');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'Elimination', function($stateParams, Elimination) {
                    return Elimination.get({id : $stateParams.id}).$promise;
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
        .state('elimination-detail.edit', {
            parent: 'elimination-detail',
            url: '/elimination/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/elimination/elimination-dialog.html',
                    controller: 'EliminationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Elimination', function(Elimination) {
                            return Elimination.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('elimination.new', {
            parent: 'tournament',
            url: '/elimination-new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/elimination/elimination-dialog.html',
                    controller: 'EliminationDialogController',
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
                                numberOfSets: null,
                                setsToWin: null,
                                tiesAllowed: null,
                                scoreMax: null,
                                type: null,
                                bronzeMatch: null,
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
        .state('elimination.edit', {
            parent: 'tournament',
            url: '/elimination/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/elimination/elimination-dialog.html',
                    controller: 'EliminationDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['Elimination', function(Elimination) {
                            return Elimination.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('tournament', null, { reload: 'tournament' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('elimination.delete', {
            parent: 'tournament',
            url: '/elimination/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/elimination/elimination-delete-dialog.html',
                    controller: 'EliminationDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['Elimination', function(Elimination) {
                            return Elimination.get({id : $stateParams.id}).$promise;
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
