import React from 'react';
import { Award, RotateCcw, ShieldCheck, Skull, Users } from 'lucide-react';
import { AvatarCatalog } from '../data/avatarCatalog';
import { GameState, PlayerRole, RoundOutcome } from '../types/game';
import { CharacterAvatarThumbnail } from './AvatarCardGraphics';
import { GoldCtaButton } from './ImpostorWizardComponents';
import { RoundCelebrationFx } from './RoundCelebrationFx';

interface RoundOutcomeScreenProps {
  gameState: GameState;
  onPlayAgain: () => void;
  onBackToLobby: () => void;
}

export const RoundOutcomeScreen: React.FC<RoundOutcomeScreenProps> = ({
  gameState,
  onPlayAgain,
  onBackToLobby,
}) => {
  const isVictory = gameState.roundOutcome === RoundOutcome.VICTORY;
  const impostors = gameState.players.filter(
    (p) => gameState.playerRoles[p.id] === PlayerRole.IMPOSTOR
  );
  const maxVotes = Math.max(
    1,
    ...gameState.players.map((p) => gameState.totalVotesPerPlayer[p.id] ?? 0)
  );

  return (
    <div className="mx-auto flex w-full max-w-2xl flex-col gap-5 pb-10">
      <RoundCelebrationFx outcome={isVictory ? 'agents' : 'impostor'} />

      {/* Victory / Defeat Hero Card */}
      <div
        className={`relative overflow-hidden rounded-3xl border-2 p-6 text-center ${
          isVictory
            ? 'animate-[verdictPop_520ms_cubic-bezier(0.22,1,0.36,1)]'
            : 'animate-[screenShake_650ms_cubic-bezier(0.36,0.07,0.19,0.97)]'
        }`}
        style={{
          borderColor: isVictory ? '#1AE682' : '#FF3E5B',
          background: isVictory
            ? 'linear-gradient(160deg, #122C24 0%, #1D1626 100%)'
            : 'linear-gradient(160deg, #341624 0%, #1D1626 100%)',
        }}
      >
        <div
          className="mx-auto flex h-16 w-16 items-center justify-center rounded-full border-2"
          style={{
            borderColor: isVictory ? '#1AE682' : '#FF3E5B',
            backgroundColor: '#16101E',
            color: isVictory ? '#5CFFB0' : '#FF7A8F',
          }}
        >
          {isVictory ? (
            <ShieldCheck className="h-8 w-8" />
          ) : (
            <Skull className="h-8 w-8" />
          )}
        </div>

        <div
          className="mt-3 font-mono-label text-xs font-extrabold uppercase tracking-[0.24em]"
          style={{ color: isVictory ? '#5CFFB0' : '#FF7A8F' }}
        >
          {isVictory ? 'AGENTS WINS!' : 'IMPOSTOR SURVIVES!'}
        </div>

        <h2 className="mt-1 font-display text-3xl font-black text-[#F7F2EC]">
          {isVictory ? 'Impostor Exposed!' : 'Impostors Win the Round'}
        </h2>
        <p className="mt-2 text-sm font-medium text-[#B7ABC8]">
          {gameState.outcomeReason}
        </p>
      </div>

      {/* Classified Debrief */}
      <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-5">
        <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.22em] text-[#F4CD6C]">
          CLASSIFIED DEBRIEF
        </div>

        <div className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2">
          <div className="rounded-2xl border border-[#423554] bg-[#16101E] p-4">
            <div className="font-mono-label text-[10px] font-bold uppercase tracking-wider text-[#887B9B]">
              SECRET WORD
            </div>
            <div className="mt-1 font-display text-2xl font-black text-[#F4CD6C]">
              {gameState.locationWord?.word ?? '—'}
            </div>
            {gameState.locationWord && (
              <div className="mt-1 text-xs font-semibold text-[#B7ABC8]">
                {gameState.locationWord.category} &bull;{' '}
                {gameState.locationWord.genre}
              </div>
            )}
          </div>

          <div className="rounded-2xl border border-[#FF3E5B]/45 bg-[#341624]/60 p-4">
            <div className="font-mono-label text-[10px] font-bold uppercase tracking-wider text-[#FF7A8F]">
              IMPOSTOR{impostors.length > 1 ? 'S' : ''}
            </div>
            <div className="mt-2 flex flex-wrap gap-2">
              {impostors.map((imp) => {
                const char = AvatarCatalog.findById(imp.avatarId);
                return (
                  <div
                    key={imp.id}
                    className="inline-flex items-center gap-2 rounded-xl border border-[#FF3E5B]/50 bg-[#16101E] px-2.5 py-1.5"
                  >
                    <CharacterAvatarThumbnail
                      character={char}
                      pose={imp.avatarPose}
                      size={24}
                      borderColor="#FF3E5B"
                    />
                    <span className="text-xs font-extrabold text-[#F7F2EC]">
                      {imp.name}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>

      {/* Total Votes Per Player */}
      <div className="rounded-3xl border border-[#423554] bg-[#241C2F] p-5">
        <div className="flex items-center justify-between">
          <div className="font-mono-label text-[10px] font-bold uppercase tracking-[0.22em] text-[#B7ABC8]">
            TOTAL VOTES PER PLAYER
          </div>
          <Award className="h-4 w-4 text-[#F4CD6C]" />
        </div>

        <div className="mt-4 space-y-3">
          {gameState.players.map((player) => {
            const votes = gameState.totalVotesPerPlayer[player.id] ?? 0;
            const isImp = gameState.playerRoles[player.id] === PlayerRole.IMPOSTOR;
            const isEliminated = gameState.votedOutPlayerIds.includes(player.id);
            const pct = Math.round((votes / maxVotes) * 100);
            const char = AvatarCatalog.findById(player.avatarId);

            return (
              <div
                key={player.id}
                className="rounded-2xl border border-[#423554] bg-[#16101E] p-3.5"
              >
                <div className="flex items-center justify-between gap-3">
                  <div className="flex items-center gap-2.5">
                    <CharacterAvatarThumbnail
                      character={char}
                      pose={player.avatarPose}
                      size={36}
                      borderColor={isImp ? '#FF3E5B' : '#1AE682'}
                    />
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-display text-sm font-extrabold text-[#F7F2EC]">
                          {player.name}
                        </span>
                        <span
                          className={`rounded-md px-1.5 py-0.5 font-mono-label text-[9px] font-extrabold uppercase ${
                            isImp
                              ? 'bg-[#341624] text-[#FF7A8F]'
                              : 'bg-[#122C24] text-[#5CFFB0]'
                          }`}
                        >
                          {isImp ? 'IMPOSTOR' : 'AGENT'}
                        </span>
                        {isEliminated && (
                          <span className="rounded-md bg-[#FF3E5B]/25 px-1.5 py-0.5 font-mono-label text-[9px] font-bold uppercase text-[#FF7A8F]">
                            VOTED OUT
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                  <span className="font-display text-base font-extrabold text-[#F4CD6C]">
                    {votes} vote{votes === 1 ? '' : 's'}
                  </span>
                </div>

                <div className="mt-2.5 h-2 w-full overflow-hidden rounded-full bg-[#241C2F]">
                  <div
                    className="h-full rounded-full transition-all duration-500"
                    style={{
                      width: `${pct}%`,
                      backgroundColor: isImp ? '#FF3E5B' : '#F4CD6C',
                    }}
                  />
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Footer Actions */}
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <button
          type="button"
          onClick={onBackToLobby}
          className="inline-flex h-[54px] items-center justify-center gap-2 rounded-2xl border border-[#423554] bg-[#241C2F] font-display text-sm font-extrabold text-[#F7F2EC] transition hover:border-[#F4CD6C]/60"
        >
          <Users className="h-4 w-4" />
          <span>Lobby</span>
        </button>
        <GoldCtaButton
          text="Play Again"
          onClick={onPlayAgain}
          icon={<RotateCcw className="h-4 w-4" />}
        />
      </div>
    </div>
  );
};
