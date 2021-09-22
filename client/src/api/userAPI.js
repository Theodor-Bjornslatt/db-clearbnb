export const createUser = async (userObject) => {
  const response = await fetch(`/api/register`, {
    method: 'POST',
    body: JSON.stringify(userObject)
  })
  if (!response.ok) return null
  const user = await response.json()
  return user
}

export const fetchUsers = async () => {
  const users = await (await fetch(`/rest/users`)).json()
  return users
}

export const fetchUserReviews = async (userId) => {
  const reviews = await (await fetch(`/rest/users/${userId}/reviews`)).json()
  return reviews
}

export const getActiveUser = async () => {
  const response = await fetch(`/api/whoami`, {
    credentials: 'include'
  })

  if (!response.ok) return null

  const userSession = await response.json()
  return userSession
}

export const loginUser = async (userLogin) => {
  const response = await fetch(`/api/login`, {
    body: JSON.stringify(userLogin),
    method: 'POST'
  })
  if (!response.ok) return null
  const user = await response.json()
  return user
}

export const logoutUser = async (userLogout) => {
  const response = await fetch(`/api/logout`, {
    credentials: 'include',
    method: 'DELETE'
  })
  if (!response.ok) return null
  return 'OK'
}
