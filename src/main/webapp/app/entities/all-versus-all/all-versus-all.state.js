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
            url: '/all-versus-all?page&sort&search',
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
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('tournament');
                    $translatePartialLoader.addPart('setSettings');
                    $translatePartialLoader.addPart('allVersusAll');
                    $translatePartialLoader.addPart('game');
                    $translatePartialLoader.addPart('gameSet');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('all-versus-all-detail', {
            parent: 'tournament', //wtf?
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
                },
                'evaluation-table@all-versus-all-detail':{
                    templateUrl: 'app/components/my/evaluation-table/evaluation-table.html',
                    controller: 'EvaluationTableController',
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
                        name: $state.current.name || 'all-versus-all',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('combined-all-versus-all-detail', {
            parent: 'combined-detail', 
            url: '/all-versus-all/{subTournamentId}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'tournamentControlApp.allVersusAll.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/all-versus-all/all-versus-all-detail.html',
                    controller: 'AllVersusAllDetailController',
                    controllerAs: 'vm'
                },
                'evaluation-table@all-versus-all-detail':{
                    templateUrl: 'app/components/my/evaluation-table/evaluation-table.html',
                    controller: 'EvaluationTableController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('allVersusAll');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'AllVersusAll', function($stateParams, AllVersusAll) {
                    return AllVersusAll.get({id : $stateParams.subTournamentId}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'all-versus-all',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('all-versus-all-detail.edit', {
            parent: 'all-versus-all-detail',
            url: '/detail/edit',
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
            parent: 'all-versus-all',
            url: '/create/new',
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
                                pointsForWinning: 1,
                                pointsForLosing: 0,
                                pointsForTie: 0.5,
                                created: null,
                                numberOfMutualMatches: null,
                                numberOfSets: null,
                                setsToWin: 1,
                                tiesAllowed: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('all-versus-all', null, { reload: 'all-versus-all' });
                }, function() {
                    $state.go('all-versus-all');
                });
            }]
        })
        .state('all-versus-all.edit', {
            parent: 'all-versus-all',
            url: '/{id}/edit',
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
                    $state.go('all-versus-all', null, { reload: 'all-versus-all' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('all-versus-all.delete', {
            parent: 'all-versus-all',
            url: '/{id}/delete',
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
                    $state.go('all-versus-all', null, { reload: 'all-versus-all' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
