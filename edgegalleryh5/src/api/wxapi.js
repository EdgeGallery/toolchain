import axios from 'axios'
function getDataApi(userName) {
  return new Promise((resolve, reject) => {
    let baseURL = '/api'
    let path = baseURL + '/gitee_test01/_search'
    axios.post(path,
      {'query': {
        'bool': {
          'must': [
            {
              'term': {
                'author_name': userName
              }
            },
            {
              'term': {
                'is_gitee_star': 1
              }
            },
            {
              'range': {
                'created_at': {
                  'gte': '2020-01-01',
                  'lte': '2021-07-03'
                }
              }
            }
          ]
        }}}
    ).then((res) => {
      resolve(res)
    }).catch(error => {
      reject(error)
    })
  })
}
function getPr(userName) {
  return new Promise((resolve, reject) => {
    let baseURL = '/api'
    let path = baseURL + '/gitee_test01/_search'
    axios.post(path,
      {
        'query':{
          'bool': {
            'must': [
              {
                'match': {
                  'user_login': userName
                }
              },
              {
                'term': {
                  'is_gitee_pull_request': 1
                }
              }
            ]
          }
        },
        'sort' : [
          {'created_at' : 'asc'}
        ]
      }
    ).then((res) => {
      resolve(res)
    }).catch(error => {
      reject(error)
    })
  })
}
function getComments(userName) {
  return new Promise((resolve, reject) => {
    let baseURL = '/api'
    let path = baseURL + '/gitee_test01/_search'
    axios.post(path,
      {
        'query':{
          'bool': {
            'must': [
              {
                'match': {
                  'user_login': userName
                }
              },
              {
                'term': {
                  'is_gitee_review_comment': 1
                }
              }
            ]
          }
        },
        'sort' : [
          {'created_at' : 'asc'}
        ]
      }
    ).then((res) => {
      resolve(res)
    }).catch(error => {
      reject(error)
    })
  })
}
function getIssue(userName) {
  return new Promise((resolve, reject) => {
    let baseURL = '/api'
    let path = baseURL + '/gitee_test01/_search'
    axios.post(path,
      {
        'query':{
          'bool': {
            'must': [
              {
                'match': {
                  'user_login': userName
                }
              },
              {
                'term': {
                  'is_bug_issue': 1
                }
              }
            ]
          }
        },
        'sort' : [
          {'created_at' : 'asc'}
        ]
      }
    ).then((res) => {
      resolve(res)
    }).catch(error => {
      reject(error)
    })
  })
}
function getDemoNum(userName) {
  return new Promise((resolve, reject) => {
    let baseURL = '/api'
    let path = baseURL + '/code_statistics/_search'
    axios.post(path,
      {
        'size': 0,
        'query':{
          'bool': {
            'must': [
              {
                'match': {
                  'author': userName
                }
              },
              {
                'term': {
                  'is_git_commit': 1
                }
              },
              {
                'range': {
                  'created_at': {
                    'gte': '2020-01-01T00:00:00+08:00',
                    'lte': '2021-12-31T23:59:59+08:00'
                  }
                }
              }
            ]
          }
        },
        'aggs': {
          'sum': {
            'sum': {
              'field': 'add'
            }
          }
        }
      }
    ).then((res) => {
      resolve(res)
    }).catch(error => {
      reject(error)
    })
  })
}
function getMeet(userName) {
  return new Promise((resolve, reject) => {
    let baseURL = '/api'
    let path = baseURL + '/gitee_test01/_search'
    axios.post(path,
      {
        'query':{
          'match': {
            'user_login': userName
          }
        },
        'sort' : [
          {'created_at' : 'asc'}
        ]
      }
    ).then((res) => {
      resolve(res)
    }).catch(error => {
      reject(error)
    })
  })
}
function getStar(userName) {
  return new Promise((resolve, reject) => {
    let baseURL = '/api'
    let path = baseURL + '/gitee_test01/_search'
    axios.post(path,
      {
        'query':{
          'bool': {
            'must': [
              {
                'match': {
                  'user_login': userName
                }
              },
              {
                'term': {
                  'is_gitee_star': 1
                }
              }
            ]
          }
        },
        'sort' : [
          {'created_at' : 'asc'}
        ]
      }
    ).then((res) => {
      resolve(res)
    }).catch(error => {
      reject(error)
    })
  })
}
function getFork(userName) {
  return new Promise((resolve, reject) => {
    let baseURL = '/api'
    let path = baseURL + '/gitee_test01/_search'
    axios.post(path,
      {
        'query':{
          'bool': {
            'must': [
              {
                'match': {
                  'user_login': userName
                }
              },
              {
                'term': {
                  'is_gitee_fork': 1
                }
              }
            ]
          }
        },
        'sort' : [
          {'created_at' : 'asc'}
        ]
      }
    ).then((res) => {
      resolve(res)
    }).catch(error => {
      reject(error)
    })
  })
}
function getTop() {
  return new Promise((resolve, reject) => {
    let baseURL = '/api'
    let path = baseURL + '/gitee_test01/_search'
    axios.post(path,
      {
        'query': {
          'bool': {
            'should': [
              {
                'term':{
                  'is_bug_issue':{
                    'value':1
                  }
                }
              },
              {'term':{
                'is_gitee_pull_request':{
                  'value':1
                }
              }
              },
              {'term':{
                'is_gitee_review_comment':{
                  'value':1
                }
              }
              },
              {'term':{
                'is_gitee_watch':{
                  'value':1
                }
              }
              },
              {'term':{
                'is_gitee_star':{
                  'value':1
                }
              }
              },
              {'term':{
                'is_gitee_fork':{
                  'value':1
                }
              }
              }]
          }
        },
        'size': 0,
        'aggs': {
          'uniq_gender': {
            'terms': {
              'field': 'user_login.keyword',
              'size': 2000
            }
          }
        }
      }
    ).then((res) => {
      resolve(res)
    }).catch(error => {
      reject(error)
    })
  })
}
export {
  getDataApi,
  getPr,
  getComments,
  getIssue,
  getDemoNum,
  getMeet,
  getStar,
  getFork,
  getTop
}
