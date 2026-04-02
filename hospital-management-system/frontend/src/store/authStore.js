import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const useAuthStore = create(persist(
  (set) => ({
    isAuth: false,
    user: null,
    token: null,

    login: (data) => {
      localStorage.setItem('jwt_token', data.accessToken);
      set({ isAuth: true, user: data, token: data.accessToken });
    },

    logout: () => {
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('user');
      set({ isAuth: false, user: null, token: null });
    },

    updateUser: (updates) =>
      set((state) => ({ user: { ...state.user, ...updates } })),
  }),
  { name: 'auth-storage', partialize: (s) => ({ isAuth: s.isAuth, user: s.user, token: s.token }) }
));

export default useAuthStore;
