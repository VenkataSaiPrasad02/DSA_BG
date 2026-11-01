const TopicIcon = ({ name }) => {
    const label = (name || 'Topic').trim();
    const initials = label
        .split(/\s+/)
        .slice(0, 2)
        .map((word) => word.charAt(0))
        .join('')
        .toUpperCase();
    let hash = 0;
    for (let index = 0; index < label.length; index += 1) {
        hash = (hash * 31 + label.charCodeAt(index)) % 360;
    }

    return (
        <span
            className="practice-topic-icon"
            style={{ '--topic-hue': hash }}
            aria-hidden="true"
        >
            {initials}
        </span>
    );
};

export default TopicIcon;
